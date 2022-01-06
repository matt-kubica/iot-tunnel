from pymodbus.client.sync import ModbusTcpClient as ModbusClient
from pymodbus.exceptions import ConnectionException, ModbusException
from pymodbus.payload import BinaryPayloadDecoder
from pymodbus.constants import Endian

import requests, time, os, logging, socket, struct

DYNAMIC_ADDRESSES = not (os.getenv('LOCAL_DEVELOPMENT', 'False') == 'True')
REQUEST_INTERVAL = int(os.getenv('REQUEST_INTERVAL', 5))
PROBE_COUNT = int(os.getenv('PROBE_COUNT', 10))

logging.basicConfig()
log = logging.getLogger('requester-service')
log.setLevel(logging.DEBUG)


def int2ip(addr):
    return socket.inet_ntoa(struct.pack("!I", addr))

def get_dynamic_addresses():
    # retrieve available gateway addresses from management service
    log.debug('Obtaining accessible gateway ip addresses...')
    while True:
        gateways = requests \
                .get('http://{0}:{1}/gateway'.format(
                    os.getenv('MANAGEMENT_SERVICE_ADDRESS', 'localhost'),
                    os.getenv('MANAGEMENT_SERVICE_PORT', 8080))) \
                .json()
        log.debug('Obtained: {0}'.format(gateways))
        time.sleep(10)
        if len(gateways) > 0: break
    return map(lambda x: x['ipAddress'], gateways)

# instantiate clients
addresses = get_dynamic_addresses() if DYNAMIC_ADDRESSES else ['127.0.0.1']
clients = [ModbusClient(address, port=5020) for address in addresses]

# wait until clients are accessible
log.debug('Waiting for clients to become accessible...')
while True:
    responses = [client.connect() for client in clients]
    if all(responses): break
    else: time.sleep(10)
log.info('Clients connected -> {0}'.format(clients))

# request clients periodically
counter, valid = 0, 0
while counter < PROBE_COUNT:

    for client in clients:
        try:
            response = client.read_holding_registers(0, 2, unit=0x00)
            value = BinaryPayloadDecoder \
                        .fromRegisters(response.registers, byteorder=Endian.Little, wordorder=Endian.Little) \
                        .decode_32bit_uint()

            if int2ip(value) == client.host:
                log.debug('Valid response from {0} -> {1} -> {2}'.format(client, value, int2ip(value)))
                valid += 1
            else: log.error('Received {0} instead of {1}'.format(int2ip(value), client.host))

        except ModbusException as exc:
            log.error('ModbusException occurred: {0}'.format(exc))

    time.sleep(REQUEST_INTERVAL)
    counter += 1


if counter * len(clients) == valid: log.info('SUCCESSFUL - all {0} probes have been valid!'.format(valid))
else: log.error('FAILED - {0}/{1} have been valid'.format(valid, counter))
