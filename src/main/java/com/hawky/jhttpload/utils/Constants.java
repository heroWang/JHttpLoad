package com.hawky.jhttpload.utils;

public interface Constants {
	int CNST_FREE = 0;
	int CNST_CONNECTING = 1;
	int CNST_READING = 2;
	
	int START_MODE_RATE = 0;
	int START_MODE_PARALLEL=1;
	int END_MODE_FETCHES = 2;
	int END_MODE_SECONDS = 3;
	
	int MAX_CONNECTIONS = 3000;
	int DEFAULT_FETCH_RATE = 1;
	int DEFAULT_TIMEOUT_SECONDS = 60;
	int DEFAULT_PARALLEL_NUMBER = 1;
	int DEFAULT_SECONDS = 60;
	int DEFAULT_FETCH_NUMBER = 10;
}
