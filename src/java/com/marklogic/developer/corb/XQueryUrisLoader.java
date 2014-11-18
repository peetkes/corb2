package com.marklogic.developer.corb;

import java.util.Properties;

import com.marklogic.developer.SimpleLogger;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.RequestOptions;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.types.XSInteger;

public class XQueryUrisLoader implements UrisLoader {
	TransformOptions options;	
	ContentSource cs;
	String collection;
	Properties properties;
	
	Session session;
	ResultSequence res;
	
	String batchRef;
	int total=0;
	
	SimpleLogger logger;
	
	String[] replacements = new String[0];
	
	public XQueryUrisLoader(){		
	}
	
	public void setOptions(TransformOptions options){
		this.options = options;
	}
	
	public void setContentSource(ContentSource cs){
		this.cs = cs;
	}
	
	public void setCollection(String collection){
		this.collection = collection;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public void open() throws CorbException{
		configureLogger();
		if(properties.containsKey("URIS-REPLACE-PATTERN")){
			String pattern = properties.getProperty("URIS-REPLACE-PATTERN").trim(); 
			replacements = pattern.split(",",-1);
			if(replacements.length % 2 != 0) throw new IllegalArgumentException("Invalid replacement pattern " + pattern);
		}
		
		try{
			RequestOptions opts = new RequestOptions();
			opts.setCacheResult(false);
	        // this should be a noop, but xqsync does it
	        opts.setResultBufferSize(0);
	        logger.info("buffer size = " + opts.getResultBufferSize()+ ", caching = " + opts.getCacheResult());
	        
			session = cs.newSession();
			
			String urisModule = options.getModuleRoot() + options.getUrisModule();
	        logger.info("invoking module " + urisModule);
	        Request req = session.newModuleInvoke(urisModule);
	        // NOTE: collection will be treated as a CWSV
	        req.setNewStringVariable("URIS", collection);
	        // TODO support DIRECTORY as type
	        req.setNewStringVariable("TYPE", TransformOptions.COLLECTION_TYPE);
	        req.setNewStringVariable("PATTERN", "[,\\s]+");
	        
	        //custom inputs
	        for(String propName:properties.stringPropertyNames()){
	        	if(propName.startsWith("URIS-MODULE.")){
	        		String varName = propName.substring("URIS-MODULE.".length());
	        		String value = properties.getProperty(propName);
	        		req.setNewStringVariable(varName, value);
	        	}
	        }
	        
	        req.setOptions(opts);
	        
	        res = session.submitRequest(req);
	        ResultItem next = res.next();
	        if(!(next.getItem() instanceof XSInteger)){
	        	batchRef = next.asString();
	        	next = res.next();
	        }
			
	        total = ((XSInteger) next.getItem()).asPrimitiveInt();
		}catch(RequestException exc){
			throw new CorbException("While invoking Uris Module",exc);
		}
	}

	public String getBatchRef() {
		return this.batchRef;
	}

	public int getTotalCount() {
		return this.total;
	}
	
	public boolean hasNext() throws CorbException {
		return res != null & res.hasNext();
	}

	public String next() throws CorbException {
		String next = res.next().asString();
		for(int i=0; i<replacements.length-1; i=i+2){
			next = next.replaceAll(replacements[i], replacements[i+1]);
		}
		return next;
	}

	@Override
	public void close() {
		if(session != null){
			logger.info("closing uris session");
			try{
				if(res != null){
					res.close();
					res = null;
				}
			}finally{
				session.close();
				session = null;
			}
		}
	}
	
	private void configureLogger() {
        if (logger == null) {
            logger = SimpleLogger.getSimpleLogger();
        }
        Properties props = new Properties();
        props.setProperty("LOG_LEVEL", options.getLogLevel());
        props.setProperty("LOG_HANDLER", options.getLogHandler());
        logger.configureLogger(props);
    }

}