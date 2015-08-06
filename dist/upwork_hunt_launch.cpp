#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <iostream>

#define RUN_DAEMON() if(run_daemon() == EXIT_FAILURE) \
	exit(EXIT_FAILURE);

pid_t sid, pid;

int run_daemon() {
	if((pid = fork()) < 0)
		return EXIT_FAILURE;	
	
	umask(0);	
	
	if((sid = setsid())<0)
		return EXIT_FAILURE;

	close(STDIN_FILENO);
	close(STDOUT_FILENO);
	close(STDERR_FILENO);
	
	return 0;
}

int main(){
	RUN_DAEMON();
	
	// Launch the Upwork bot:
	system("./upwork_hunt.jar");

	while(1){
		// Manage the java process here, kill it if necessary
		sleep(5);
	}	

	exit(EXIT_SUCCESS);
	return 0;
}
