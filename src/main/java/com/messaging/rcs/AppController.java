package com.messaging.rcs;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.messaging.rcs.util.SystemConstants;

/**
 * 
 * @author Rahul 2023-04-10
 *
 */

@CrossOrigin
@Controller
@RequestMapping(value = SystemConstants.BASE_URL, produces = {APPLICATION_JSON_VALUE})
public class AppController {
    @GetMapping("/versionInfo")
    public
    @ResponseBody
    ResponseEntity versionInfo() {
        BuildVersionInfo info = new BuildVersionInfo();
        info.setBranch("Master");
        info.setBuildVersion("1.0.0");
        info.setLastCommitDate("2023-April-10");
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate()).body(info);
    }
}
