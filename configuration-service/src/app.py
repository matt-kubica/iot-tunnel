import random

from apscheduler.schedulers.background import BackgroundScheduler

import ansible_runner, requests
import os, socket, time, logging, pathlib

logging.basicConfig()
log = logging.getLogger('configuration-service')
log.setLevel(logging.DEBUG)


class Configurator:

    def __init__(self, playbook_path, scheduler, extra_vars, cron_params):
        self.scheduler = scheduler
        self.playbook_path = playbook_path
        self.extra_vars = extra_vars
        self.cron_params = cron_params
        self.inventory = {
            'clients': {
                'hosts': { },
                'vars': {
                    # TODO: this should be changed to use ssh key instead
                    'ansible_connection': 'ssh',
                    'ansible_ssh_user': 'pi',
                    'ansible_ssh_pass': 'raspberry'
                }
            }
        }
        self.scheduler.add_job(func=self._update_hosts, trigger='cron', **self.cron_params)


    def _get_gateway_addresses(self):
        res = requests.get('http://{0}:{1}/gateway'.format(
            self.extra_vars['MANAGEMENT_SERVICE_ADDRESS'],
            self.extra_vars['MANAGEMENT_SERVICE_PORT']
        ))

        if res.status_code != 200: return []
        else: return list(map(lambda obj: obj['ipAddress'], res.json()))

    def _update_hosts(self):
        updated_addresses = self._get_gateway_addresses()

        self.inventory['clients']['hosts'] = dict.fromkeys(updated_addresses, None)
        log.debug('Updated inventory with: {0}'.format(updated_addresses))
        log.debug('Playbook to be executed: {0}'.format(self.playbook_path))
        res = ansible_runner.interface.run(playbook=self.playbook_path, inventory=self.inventory, extravars=self.extra_vars)
        log.info('Update status: {0}'.format(res.status))

    def run(self):
        self.scheduler.start()
        log.info('Started configurator...')
        while True: time.sleep(1)



if __name__ == '__main__':
    cron_params = {
        'hour': str(random.randint(0, 24)),
        'minute': str(random.randint(0, 60)),
        'second': str(random.randint(0, 60)),
    }
    log.info('Random cron params for configurator: {0}'.format(cron_params))

    configurator = Configurator(
        playbook_path=str(pathlib.Path(__file__).parents[1].joinpath('ansible/periodical-update.yaml')),
        scheduler=BackgroundScheduler(timezone='Europe/Warsaw'),
        extra_vars={
            'MANAGEMENT_SERVICE_ADDRESS': socket.gethostbyname('iot-tunnel-management-service'),
            'MANAGEMENT_SERVICE_PORT': os.getenv('MANAGEMENT_SERVICE_PORT', '8080'),
            'OVPN_INTERNAL_SERVER_ADDRESS': os.getenv('OVPN_INTERNAL_SERVER_ADDRESS', '10.8.0.1')
        },
        cron_params=cron_params
    )
    configurator.run()



