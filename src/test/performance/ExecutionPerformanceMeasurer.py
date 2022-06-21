import datetime
import logging
import subprocess
import sys
from typing import Tuple

import matplotlib.pyplot as plt
import numpy as np


def get_logger(path):
    log_formatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
    root_logger = logging.getLogger()
    file_handler = logging.FileHandler(path)
    file_handler.setFormatter(log_formatter)
    root_logger.addHandler(file_handler)

    console_handler = logging.StreamHandler()
    console_handler.setFormatter(log_formatter)
    root_logger.addHandler(console_handler)

    return root_logger


class ExecutionMeasurer:
    LOG_PATH = 'verbose.log'

    def __init__(self):
        self.logger = get_logger(self.LOG_PATH)
        self.no_optimizations_measurements = []
        self.optimizations_measurements = []

    def compile_and_run(self, path: str, package_path: str, path_to_compiled_class: str = ''):
        if path_to_compiled_class is None:
            path_to_compiled_class = ''
        success = self.compile_with_optimizations(path, path_to_compiled_class)
        if not success:
            raise 'Failed to compile program with optimizations'
        delta1 = self.run_java_and_measure(path_to_compiled_class, package_path)
        success = self.compile_without_optimizations(path, path_to_compiled_class)
        if not success:
            raise 'Failed to compile program without optimizations'
        delta2 = self.run_java_and_measure(path_to_compiled_class, package_path)
        self.optimizations_measurements.append(delta1)
        self.no_optimizations_measurements.append(delta2)

    def compile_with_optimizations(self, path: str, path_to_compiled_classes: str = '') -> bool:
        cmd = './oxma {} --optimize --outfile={}'.format(path, path_to_compiled_classes)
        return self.run_bash_cmd(cmd)

    def compile_without_optimizations(self, path: str, path_to_compiled_classes: str = '') -> bool:
        cmd = './oxma {} --outfile={}'.format(path, path_to_compiled_classes)
        return self.run_bash_cmd(cmd)

    def run_java_and_measure(self, path: str, package: str) -> float:
        start = datetime.datetime.now()
        cmd = 'java -Djava.compiler=NONE -cp {} {}'.format(path, package)
        print(cmd)
        success = self.run_bash_cmd(cmd)
        if not success:
            raise 'Failed to execute java program'
        end = datetime.datetime.now()
        res = (end - start).total_seconds()
        print(res)
        return res

    def run_bash_cmd(self, cmd) -> bool:
        process = subprocess.Popen(cmd.split())
        output, error = process.communicate()
        self.logger.info(msg=output)
        self.logger.error(msg=error)
        return error is None

    def get_measurements(self) -> Tuple[np.array, np.array]:
        """
        :return: tuple of arrays: (no optimization deltas, optimization deltas)
        """
        return np.array(self.no_optimizations_measurements), np.array(self.optimizations_measurements)


def plot_results(xs, ys):
    print(ys)
    plt.plot(xs, ys)
    plt.show()


if __name__ == '__main__':
    args = sys.argv
    paths, paths_to_compiled, paths_package, volume = [], [], [], []
    for i in range(1, len(args), 4):
        paths.append(args[i])
        paths_to_compiled.append(None if args[i + 1] == 'None' else args[i + 1])
        paths_package.append(args[i + 2])
        volume.append(args[i + 3])
    measurer = ExecutionMeasurer()
    for i in range(len(paths)):
        measurer.compile_and_run(paths[i], paths_package[i], paths_to_compiled[i])
    plot_results(
        volume,
        list(map(lambda x, y: max(0, x - y), *measurer.get_measurements()))
    )
