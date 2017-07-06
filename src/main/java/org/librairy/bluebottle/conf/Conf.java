package org.librairy.bluebottle.conf;

import java.util.Optional;

public class Conf {
	static String endpointURL = "http://api.staging.bluebottlebiz.com/";
	static String endpointLibrairy = "http://138.100.15.128:9999/api/";
	//static String endpointLibrairy = "http://minetur.dia.fi.upm.es:9999/api/";
	static String apikey = "e62f85c0-8ed2-11e6-96d5-b8aeed74afe3";
	static String runConf = "blueBottle";
	static boolean cacheEnabled = false;
	static boolean loadGT= false;
	static String tokenizerMode = "lemma+compound";
	static int ldaDelay = 660000;
	static int w2vDelay = 600000;
	static int forcePages = 0;
	static int startPage = 0;
	static Optional<Integer> topics = Optional.empty();

	static boolean parallelProcessing = false;
	  
	  
	public static String getRunConf() {
		return runConf;
	}
	public static void setRunConf(String runConf) {
		Conf.runConf = runConf;
	}
	public static String getEndpointURL() {
		return endpointURL;
	}
	public static void setEndpointURL(String endpointURL) {
		Conf.endpointURL = endpointURL;
	}
	public static String getApikey() {
		return apikey;
	}
	public static void setApikey(String apikey) {
		Conf.apikey = apikey;
	}
	public static boolean isCacheEnabled() {
		return cacheEnabled;
	}
	public static void setCacheEnabled(boolean cacheEnabled) {
		Conf.cacheEnabled = cacheEnabled;
	}
	public static boolean getLoadGT() {
		// TODO Auto-generated method stub
		return loadGT;
	}
	public static String getTokenizerMode() {
		return tokenizerMode;
	}
	public static void setTokenizerMode(String tokenizerMode) {
		Conf.tokenizerMode = tokenizerMode;
	}
	public static int getLdaDelay() {
		return ldaDelay;
	}
	public static void setLdaDelay(int ldaDelay) {
		Conf.ldaDelay = ldaDelay;
	}
	public static int getW2vDelay() {
		return w2vDelay;
	}
	public static void setW2vDelay(int w2vDelay) {
		Conf.w2vDelay = w2vDelay;
	}
	public static void setLoadGT(boolean loadGT) {
		Conf.loadGT = loadGT;
	}
	public static int getForcePages() {
		return forcePages;
	}
	public static void setForcePages(int forcePages) {
		Conf.forcePages = forcePages;
	}
	public static boolean isParallelProcessing() {
		return parallelProcessing;
	}
	public static void setParallelProcessing(boolean parallelProcessing) {
		Conf.parallelProcessing = parallelProcessing;
	}
	public static String getEndpointLibrairy() {
		return endpointLibrairy;
	}
	public static void setEndpointLibrairy(String endpointLibrairy) {
		Conf.endpointLibrairy = endpointLibrairy;
	}
	public static int getStartPage() {
		return startPage;
	}
	public static void setStartPage(int startPage) {
		Conf.startPage = startPage;
	}
	public static Optional<Integer> getTopics(){return topics;}
	public static void setTopics(int topics){ Conf.topics = Optional.of(topics);}
	
	

}
