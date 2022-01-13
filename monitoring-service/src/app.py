from apscheduler.schedulers.background import BlockingScheduler

from pymodbus.client.sync import ModbusTcpClient as ModbusClient
from pymodbus.payload import BinaryPayloadDecoder
from pymodbus.constants import Endian
from pymodbus.pdu import ExceptionResponse

import requests, logging, os, socket

DYNAMIC = os.getenv('DYNAMIC_ADDRESSES', 'FALSE') != 'TRUE'
STATIC_ADDRESSES = ['192.168.1.121']

logging.basicConfig()
log = logging.getLogger('monitoring-service')
log.setLevel(logging.DEBUG)


def get_dynamic_addresses():
    log.debug('Obtaining accessible gateway ip addresses...')
    gateways = requests \
        .get('http://{0}:{1}/gateway'.format(
            socket.gethostbyname('iot-tunnel-management-service'),
            os.getenv('MANAGEMENT_SERVICE_PORT', 8080))) \
        .json()
    return list(map(lambda x: x['ipAddress'], gateways))


def request_routine():
    addresses = get_dynamic_addresses() if DYNAMIC else STATIC_ADDRESSES
    clients = [ModbusClient(address, port=5020) for address in addresses]
    statuses = {client: client.connect() for client in clients}

    for client, status in statuses.items():
        if status:
            response = client.read_holding_registers(0, 2)
            if type(response) is not ExceptionResponse:
                value = BinaryPayloadDecoder \
                    .fromRegisters(response.registers, byteorder=Endian.Little, wordorder=Endian.Little) \
                    .decode_32bit_uint()
                log.info('Value retrieved from {0} -> {1}'.format(client.host, value))
            else:
                log.error('Received exception response: {0}'.format(response))
            client.close()
        else:
            log.error('Cannot connect with: {0}'.format(client.host))


if __name__ == '__main__':
    cron_params = {
        'hour': os.getenv('CRON_HOUR', '*'),
        'minute': os.getenv('CRON_MINUTE', '*'),
        'second': os.getenv('CRON_SECOND', '0'),
    }

    scheduler = BlockingScheduler(timezone='Europe/Warsaw')
    scheduler.add_job(func=request_routine, trigger='cron', **cron_params)

    log.info('Starting service\'s scheduler with cron params: {0}'.format(cron_params))
    scheduler.start()

