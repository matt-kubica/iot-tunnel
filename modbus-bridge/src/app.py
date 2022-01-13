import random, argparse, sys

from pymodbus.server.asynchronous import StartTcpServer
from pymodbus.datastore import ModbusServerContext
from pymodbus.datastore.remote import RemoteSlaveContext
from pymodbus.client.sync import ModbusSerialClient as ModbusClient
from pymodbus.interfaces import IModbusSlaveContext

import logging
logging.basicConfig()
log = logging.getLogger('bridging-service')
log.setLevel(logging.DEBUG)


class DummySlaveContext(IModbusSlaveContext):

    def getValues(self, fx, address, count=1):
        vals = [random.randint(0, 255) for _ in range(address, address + count)]
        log.debug('Received request - randomly chosen values: {0}'.format(vals))
        return vals

    def validate(self, fx, address, count=1):
        return True


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('--port', nargs='?', type=str, default='/dev/ptyp0', dest='port')
    parser.add_argument('--baud', nargs='?', type=int, default=9600, dest='baud')
    parser.add_argument('--timeout', nargs='?', type=int, default=1, dest='timeout')
    parser.add_argument('--dummy', action='store_const', const=True, default=False, dest='dummy')
    return parser.parse_args(sys.argv[1::])


def run_bridging_server(args):

    client = ModbusClient(method='rtu', port=args.port, timeout=args.timeout, baudrate=args.baud)
    store = DummySlaveContext() if args.dummy else RemoteSlaveContext(client)
    context = ModbusServerContext(slaves=store, single=True)

    if args.dummy:
        log.info('Starting ModbusTCP server in dummy mode, it will generate random values on every \'reading\' request, \'writing\' requests are not supported!')
    else:
        log.info('Starting ModbusTCP server in bridging mode, it will pass requests to slave connected to \'{0}\', with {1} baud rate...'.format(args.port, args.baud))

    StartTcpServer(context, address=('0.0.0.0', 5020))


if __name__ == "__main__":
    args = parse_args()
    run_bridging_server(args)