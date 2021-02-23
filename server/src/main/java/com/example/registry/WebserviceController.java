package com.example.registry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping(method = {RequestMethod.GET, RequestMethod.PUT})
public class WebserviceController {

    @Autowired
    private DataRepository datarepository;

    public WebserviceController() throws NoSuchAlgorithmException {
    }

    @PostMapping(value = "/registry/add", consumes = "application/json", produces = "application/json")
    public ResponseEntity<StatusResponse> add(@RequestBody UserDataRequest data) {
        if (data.getUuid().length() != 36){
            return new ResponseEntity<StatusResponse>(new StatusResponse(ResponseStatus.INVALID_REQUEST), HttpStatus.OK);
        } else {
            boolean exists = datarepository.existsByUuid(data.getUuid());
            if (!exists){
                UserDataDB userDataDB = new UserDataDB();
                userDataDB.setUuid(data.getUuid());
                userDataDB.setPublickey(data.getPublickey());
                userDataDB.setInstancetype(data.getInstancetype());
                userDataDB.setVersion(data.getVersion());
                userDataDB.setStatus(SystemStatus.PENDING);
                datarepository.save(userDataDB);
                return new ResponseEntity<StatusResponse>(new StatusResponse(ResponseStatus.OK), HttpStatus.OK);
            } else {
                return new ResponseEntity<StatusResponse>(new StatusResponse(ResponseStatus.KNOWN_UUID), HttpStatus.OK);
            }
        }
    }

    @PostMapping(value = "/registry/adopt", consumes = "application/json", produces = "application/json")
    public ResponseEntity<StatusResponse> adopt(@RequestBody AdoptRequest data) {
        if (data.getUuid().length() != 36){
            return new ResponseEntity<StatusResponse>(new StatusResponse(ResponseStatus.INVALID_REQUEST), HttpStatus.OK);
        } else {
            boolean exists = datarepository.existsByUuid(data.getUuid());
            if (!exists){
                return new ResponseEntity<StatusResponse>(new StatusResponse(ResponseStatus.UNKNOWN_UUID), HttpStatus.OK);
            } else {
                UserDataDB userData = datarepository.findByUuid(data.getUuid());
                if(data.getAdopt()){
                    userData.setStatus(SystemStatus.ADOPTED);
                } else {
                    userData.setStatus(SystemStatus.REJECTED);
                }
                datarepository.save(userData);
                return new ResponseEntity<StatusResponse>(new StatusResponse(ResponseStatus.OK), HttpStatus.OK);
            }
        }
    }

    @PostMapping(value = "/registry/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<StatusResponse> update(@RequestBody UpdateRequest data) {
        if (data.getUuid().length() != 36){
            return new ResponseEntity<StatusResponse>(new StatusResponse(ResponseStatus.INVALID_REQUEST), HttpStatus.OK);
        } else {
            boolean exists = datarepository.existsByUuid(data.getUuid());
            if (!exists){
                return new ResponseEntity<StatusResponse>(new StatusResponse(ResponseStatus.UNKNOWN_UUID), HttpStatus.OK);
            } else {
                UserDataDB userData = datarepository.findByUuid(data.getUuid());
                userData.setVersion(data.getVersion());
                datarepository.save(userData);
                return new ResponseEntity<StatusResponse>(new StatusResponse(ResponseStatus.OK), HttpStatus.OK);
            }
        }
    }

    @PostMapping(value = "/registry/list", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ListResponse> list(@RequestBody FilterRequest filter) {
        String instancetype = filter.getFilter().getInstancetype();
        SystemStatus status = filter.getFilter().getStatus();
        List<UserDataDB> userData;

        if(status.equals(SystemStatus.PENDING)){
            userData = findby(instancetype, SystemStatus.PENDING);
        } else if(status.equals(SystemStatus.ADOPTED)){
            userData = findby(instancetype, SystemStatus.ADOPTED);
        } else if(status.equals(SystemStatus.REJECTED)){
            userData = findby(instancetype, SystemStatus.REJECTED);
        } else if(!instancetype.isEmpty()){
            userData = datarepository.findByInstancetype(instancetype);
        }
        else {
            userData = datarepository.findAll();
        }

        return new ResponseEntity<ListResponse>(new ListResponse(ResponseStatus.OK, userData), HttpStatus.OK);
    }

    @PostMapping(value = "/registry/heartbeat", consumes = "application/json", produces = "application/json")
    public ResponseEntity<StatusResponse> heartbeat(@RequestBody UserDataRequest data) throws ParseException {
        UserDataDB userData = datarepository.findByUuid(data.getUuid());
        if (userData.getStatus() != SystemStatus.ADOPTED){
            return new ResponseEntity<StatusResponse>(new StatusResponse(ResponseStatus.NOT_ADOPTED), HttpStatus.OK);
        } else {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            userData.setTimestamp(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(dtf.format(now)));
            datarepository.save(userData);
            return new ResponseEntity<StatusResponse>(new StatusResponse(ResponseStatus.OK), HttpStatus.OK);
        }
    }

    public String uuid_gen() {
        try{
            Process serialnum = Runtime.getRuntime().exec("sudo cat /sys/class/dmi/id/product_uuid");
            BufferedReader reader = new BufferedReader(new InputStreamReader(serialnum.getInputStream()));
            return reader.readLine().trim();
        } catch (Exception e){
            return e.getMessage();
        }
    }

    public String key_gen() throws NoSuchAlgorithmException {
            KeyPairGenerator kg= KeyPairGenerator.getInstance("RSA");
            kg.initialize(2048);
            KeyPair kp = kg.generateKeyPair();
            Key pub_key = kp.getPublic();

            return Base64.getEncoder().encodeToString(pub_key.getEncoded());
    }

    public List<UserDataDB> findby(String instancetype, SystemStatus status){
        if(!instancetype.isEmpty()){
            return datarepository.findByInstancetypeAndStatus(instancetype, status);
        } else {
            return datarepository.findByStatus(status);
        }
    }
}
