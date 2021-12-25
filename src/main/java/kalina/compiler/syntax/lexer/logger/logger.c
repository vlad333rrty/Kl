#include "logger.h"
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

void logInfo(char* message) {
   time_t now;
   time(&now);
   printf("%s [INFO]: %s\n", ctime(&now), message);
}

void logError(char *message, ...) {
      time_t now;
      time(&now);
      printf("%s [ERROR]: %s\n", ctime(&now), message);
}