/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.bluebottle;

import org.librairy.bluebottle.conf.Conf;
import org.librairy.bluebottle.load.BlueBottleLoaderParallel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Created on 21/05/16:
 *
 * @author cbadenes
 */
//@SpringBootApplication
//@ComponentScan({"org.librairy", "io.swagger"})
//@PropertySource({"classpath:boot.properties"})
public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    static int port = 8888;

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public static EmbeddedServletContainerFactory getTomcatEmbeddedFactory(){
        TomcatEmbeddedServletContainerFactory servlet = new TomcatEmbeddedServletContainerFactory();
        servlet.setPort(port);
        return servlet;
    }

    public static void main(String[] args){
        try {

            if (args != null && args.length > 0){

                port = Integer.valueOf(args[0]);
            }
            
            loadEnvironment();
           
            LOG.info("Listening on port: " + port);

            BlueBottleLoaderParallel loader = new BlueBottleLoaderParallel();
            loader .loadBooks();
           //context.getBean(BlueBottleLoaderParallel.class).loadBooks();
           //context.getBean(SaveResources.class).deleteAll();

        } catch (Exception e) {
            LOG.error("Error executing test",e);
            System.exit(-1);
        }

    }

	private static void loadEnvironment() {
		
		
		 String varName = System.getenv("LIBRAIRY_HARVESTER_CORPUS_NAME");
		 
		 String varEndpointLibrairy = System.getenv("LIBRAIRY_HARVESTER_ENDPOINT_LIBRAIRY");
		 String varEndpoint = System.getenv("LIBRAIRY_HARVESTER_ENDPOINT_CLIENT");
		 String varApikey = System.getenv("LIBRAIRY_HARVESTER_ENDPOINT_CLIENT_APIKEY");
		 String varCache = System.getenv("LIBRAIRY_HARVESTER_CACHE_ENABLED");
		 String varLoadGT = System.getenv("LIBRAIRY_HARVESTER_LOAD_GT");
		 String varTokenizerMode = System.getenv("LIBRAIRY_HARVESTER_TOKENIZER_MODE");
		 String varLdaDelay = System.getenv("LIBRAIRY_HARVESTER_LDA_DELAY");
		 String varW2vDelay = System.getenv("LIBRAIRY_HARVESTER_W2V_DELAY");
		 String varForcePage = System.getenv("LIBRAIRY_HARVESTER_FORCE_PAGE");
		 String varStartPage = System.getenv("LIBRAIRY_HARVESTER_START_PAGE");
		 String varParallel = System.getenv("LIBRAIRY_HARVESTER_PARALLEL_PROCESSING");
 
         //String var = "testIdCorpus";

         if (varName != null && !varName.isEmpty()) 
         	Conf.setRunConf(varName);

         if (varEndpointLibrairy != null && !varEndpointLibrairy.isEmpty()) 
           	Conf.setEndpointLibrairy(varEndpointLibrairy);
         
         
         
         
         if (varApikey != null && !varApikey.isEmpty()) 
          	Conf.setApikey(varApikey);
         if (varEndpoint != null && !varEndpoint.isEmpty()) 
           	Conf.setEndpointURL(varEndpoint);
         
         
         
         
         if (varCache != null && !varCache.isEmpty()) 
          	Conf.setCacheEnabled(varCache.equals("1"));
         if (varLoadGT != null && !varLoadGT.isEmpty()) 
          	Conf.setLoadGT(varLoadGT.equals("1"));
         if (varTokenizerMode != null && !varTokenizerMode.isEmpty()) 
          	Conf.setTokenizerMode(varTokenizerMode);
         if (varLdaDelay != null && !varLdaDelay.isEmpty()) 
          	Conf.setLdaDelay(Integer.parseInt(varLdaDelay));
         if (varW2vDelay != null && !varW2vDelay.isEmpty()) 
          	Conf.setW2vDelay(Integer.parseInt(varW2vDelay));
         if (varForcePage != null && !varForcePage.isEmpty()) 
          	Conf.setForcePages(Integer.parseInt(varForcePage));
         if (varStartPage != null && !varStartPage.isEmpty()) 
           	Conf.setStartPage(Integer.parseInt(varStartPage));
         if (varParallel != null && !varParallel.isEmpty()) 
          	Conf.setParallelProcessing(varParallel.equals("1"));
         
         System.out.println("====    HAVESTING CONFIGURATION    ====");

         System.out.println("Domain Name: " + Conf.getRunConf());
         System.out.println("Librairy URL: " + Conf.getEndpointLibrairy());
         System.out.println("Endpoint URL: " + Conf.getEndpointURL());
         System.out.println("API key: " + Conf.getApikey());
         System.out.println("Cache enabled: " + Conf.isCacheEnabled());
         System.out.println("Load GT: " + Conf.getLoadGT());
         System.out.println("Tokenizer mode:  " + Conf.getTokenizerMode());
         System.out.println("LDA delay: " + Conf.getLdaDelay());
         System.out.println("W2V delay: " + Conf.getW2vDelay());
         System.out.println("Force Num Pages: " + Conf.getForcePages());
         System.out.println("Start Page: " + Conf.getStartPage());
         System.out.println("Parallel processing: " + Conf.isParallelProcessing());

         System.out.println("=======================================");

	}
}