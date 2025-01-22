package com.messaging.rcs.model;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.messaging.rcs.domain.UserEntity;
@Service
public class ClientIPValidation {
	@Autowired
	private HttpServletRequest request;
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientIPValidation.class);
 public boolean ValidateClientIP(UserEntity objclnt) throws InvalidIPException
 {
	 String remoteAddr = "";
	 if (request != null) 
	 {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
            
            if(remoteAddr != null && !remoteAddr.equals("") && remoteAddr.contains(","))
            {
            	String remoteAddrArg[] = remoteAddr.split(",");
            	for(int i=0; i< remoteAddrArg.length; i++){
            		remoteAddr = remoteAddrArg[i];
            		if(!remoteAddr.startsWith("192.168.")){
            			break;
            		}
            	}
            }
     }
	 
	 if(remoteAddr != null && !remoteAddr.equals(""))
	 {
		 remoteAddr = remoteAddr.trim();
	 }
	
	 LOGGER.info("Remote IP --- "+remoteAddr);
	 
	 String validIPs=null;//objclnt.getValid_ip();
	List<String> validIpList = Arrays.asList(validIPs.split("\\s*,\\s*"));
	 
	 LOGGER.debug("validIpList --- "+validIpList);
	 
	 LOGGER.debug("!validIpList.contains(remoteAddr) --- ");
	 if(validIpList.contains(remoteAddr)|| validIPs.isEmpty()||validIPs ==null)
	 	return true;
	 else
		 throw new InvalidIPException("");
 }
}
