from pymodbus.server.asynchronous import StartTcpServer
from pymodbus.datastore import ModbusSequentialDataBlock, ModbusSlaveContext, ModbusServerContext
from pymodbus.payload import BinaryPayloadBuilder
from pymodbus.constants import Endian

import socket, struct, netifaces, logging, os
from time import sleep

IFACE = 'lo0' if (os.getenv('LOCAL_DEVELOPMENT', 'False') == 'True') else 'tun0'

logging.basicConfig()
log = logging.getLogger('gateway-service')
log.setLevel(logging.DEBUG)


def ip2int(addr):
    return struct.unpack("!I", socket.inet_aton(addr))[0]


while IFACE not in netifaces.interfaces():
    log.debug('Waiting for \'tun\' interface...')
    log.debug('Available interfaces: {0}'.format(netifaces.interfaces()))
    sleep(5)

address_string = netifaces.ifaddresses(IFACE)[netifaces.AF_INET][0]['addr']
address = ip2int(address_string)
log.info('Ip address of \'tun0\' interface: {0} -> {1}'.format(address_string, address))

builder = BinaryPayloadBuilder(byteorder=Endian.Little, wordorder=Endian.Little)
builder.add_32bit_uint(address)

block = ModbusSequentialDataBlock(0, builder.to_registers())
store = ModbusSlaveContext(hr=block, zero_mode=True)
context = ModbusServerContext(slaves=store, single=True)
log.debug('Initialized server context with following data block: {0}'.format(block))

log.info('Asynchronous ModbusTCP server is about to start...')
StartTcpServer(context, address=(address_string, 5020))