package org.ecoinformatics.oboe.util;

/**
 * This class provides functions for debug purpose.
 * 
 * @author cao
 *
 */
public class Debugger {
	
	/**
	 * Get the function name of the caller
	 * stack[0].getMethodName() = getWhoCalledMe
	 * @return
	 */
	private static String getWhoCalledMe() {
		try {
			throw new Throwable();
		} catch ( Throwable e ) {
			StackTraceElement stack[] = e.getStackTrace();
			return (stack[1].getMethodName()+":");
		}
	}
	
	/**
	 * Get the line number the caller	 
	 * @return
	 */
	private static int getCallerLineNum() {
		try {
			throw new Throwable();
		} catch ( Throwable e ) {
			StackTraceElement stack[] = e.getStackTrace();
			return (stack[1].getLineNumber());
		}
	}
	
	/**
	 * Get the (filename:linenumber) of the caller	 
	 * @return
	 */
	public static String getCallerPosition() {
		try {
			throw new Throwable();
		} catch ( Throwable e ) {
			StackTraceElement stack[] = e.getStackTrace();
			return (stack[1].getFileName()+":"+stack[1].getLineNumber()+": "+stack[1].getMethodName()+":");
		}
	}
	
	/**
	 * Get the function name of the caller in the stack trace
	 * stack[0].getMethodName() = getStackTraceCaller
	 * @return
	 */
	private static String getStackTraceCaller() {
		try {
			throw new Throwable();
		} catch ( Throwable e ) {
			StackTraceElement stack[] = e.getStackTrace();
			String stackTraceCallerNames="";
			for(int i=stack.length-1;i>=1;i--){
				stackTraceCallerNames +=(stack[i].getMethodName()+":");				
			}
			return (stackTraceCallerNames);
		}
	}
}

