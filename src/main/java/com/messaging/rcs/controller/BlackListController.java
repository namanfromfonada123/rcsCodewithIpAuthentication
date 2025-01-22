package com.messaging.rcs.controller;

import com.google.gson.Gson;
import com.messaging.rcs.domain.BlackListEntity;
import com.messaging.rcs.domain.BlackListUploadFile;
import com.messaging.rcs.email.repository.BlackListUploadFileRepository;
import com.messaging.rcs.model.DataContainer;
import com.messaging.rcs.repository.BlackListRepository;
import com.messaging.rcs.service.BlacklistService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = {"/api/v1/rcsmessaging/blacklist"}, produces = {"application/json"})
public class BlackListController {
  public static final Logger Logger = LoggerFactory.getLogger(com.messaging.rcs.controller.BlackListController.class);
  
  @Autowired
  private BlackListUploadFileRepository blackListUploadFileRepository;
  
  @Autowired
  private BlacklistService blacklistService;
  
  @Autowired
  private BlackListRepository blackListRepository;
  
  @PostMapping({"/generateBlacklistNo"})
  public DataContainer saveBlackMsisDn(@RequestBody BlackListEntity blacklist) {
    DataContainer data = null;
    data = this.blacklistService.createBlackListNo(blacklist);
    return data;
  }
  
  @GetMapping({"/findByMsisdnAndClientid"})
  public DataContainer findByMsisdnAndClientid(@RequestParam("msisdn") String msisdn, @RequestParam("userId") Long userId) {
    System.out.println("Goning Serach By MNo::" + msisdn + "And Client Id::" + userId);
    return this.blacklistService.findByMsisdnAndClientid(msisdn, userId);
  }
  
  @GetMapping({"/deleteByMsisdnAndUserId"})
  public DataContainer deleteByMsisdnAndUserId(@RequestParam("id") Long id) {
    DataContainer data = new DataContainer();
    this.blackListRepository.deleteById(id);
    data.setMsg("Record Deleted.");
    data.setStatus(Integer.valueOf(200));
    return data;
  }
  
  @GetMapping({"/findAllBalckListNoByUserId"})
  public DataContainer findAllBalckListNoByClientId(@RequestParam("userId") Long userId) {
    System.out.println("Goning Serach By  Client Id::" + userId);
    DataContainer data = new DataContainer();
    List<BlackListEntity> blackList = null;
    try {
      blackList = this.blackListRepository.findByUserId(userId);
      if (blackList.size() > 0) {
        data.setData(blackList);
        data.setStatus(Integer.valueOf(200));
        data.setMsg("Success");
      } else {
        data.setStatus(Integer.valueOf(404));
        data.setMsg("Data Not Found.");
      } 
    } catch (Exception e) {
      data.setStatus(Integer.valueOf(500));
      data.setMsg(e.getMessage());
      e.printStackTrace();
    } 
    return data;
  }
  
  @PostMapping(value = {"/uploadBlackListNo"}, consumes = {"multipart/form-data", "application/json"})
  public String uploadBlackListNoFile(@RequestPart("file") MultipartFile file, @RequestParam("userId") Long userId) throws IOException {
    Logger.info("**** Inside BlackListRedisController.uploadBlackListFile() *****");
    DataContainer data = new DataContainer();
    CompletableFuture<Map<String, String>> responseMsg = null;
    HashedMap<String, String> hashedMap = new HashedMap();
    if ("xlsx".equals(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1)) || "csv"
      .equals(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1)) || "xls"
      .equals(file
        .getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1))) {
      try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        if (Objects.nonNull(file)) {
          List<String> fileNames = new ArrayList<>();
          String date = sdf.format(new Date());
          byte[] bytes = new byte[0];
          bytes = file.getBytes();
          Files.write(Paths.get(file.getOriginalFilename(), new String[0]), bytes, new java.nio.file.OpenOption[0]);
          BlackListUploadFile fileDb = new BlackListUploadFile();
          fileDb.setData(file.getBytes());
          fileDb.setName(file.getOriginalFilename());
          fileDb.setType(file.getContentType());
          fileDb.setUserId(userId);
          fileDb.setCreatedDate(date);
          fileDb.setIsExecute(Integer.valueOf(0));
          fileNames.add(file.getOriginalFilename());
          this.blackListUploadFileRepository.save(fileDb);
          hashedMap.put("STATUS", String.valueOf(200));
          hashedMap.put("MESSAGE", "File Upload SuccessFully");
          return (new Gson()).toJson(hashedMap).toString();
        } 
      } catch (Exception e) {
        e.printStackTrace();
        hashedMap.put("Could not upload the file", file.getOriginalFilename() + "!");
        hashedMap.put("EXCEPTION", HttpStatus.EXPECTATION_FAILED.toString());
        hashedMap.put("REQUEST_FAILED", "400");
        Logger.info("**** Inside  BlackListRedisController.uploadBlackListFile() Got Exception*****" + data
            .toString());
      } 
    } else {
      hashedMap.put("INVALID_FORMAT", "Invalid Format");
      hashedMap.put("NOT FOUND", "404");
      Logger.info("**** Inside  BlackListRedisController.uploadBlackListFile() Got Invalid Format*****" + data
          .toString());
    } 
    Logger.info("**** Successfully Executed Inside  BlackListRedisController.uploadBlackListFile() *****");
    return (new Gson()).toJson(hashedMap);
  }
}
