package com.prezi.grub;

public class GrubException extends RuntimeException {
	public GrubException() {
	}

	public GrubException(String s) {
		super(s);
	}

	public GrubException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public GrubException(Throwable throwable) {
		super(throwable);
	}
}
