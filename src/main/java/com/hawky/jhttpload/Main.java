package com.hawky.jhttpload;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		BenchMark benchMark = new BenchMark();
		benchMark.parseOptions(args);
		benchMark.run();
	}
}
