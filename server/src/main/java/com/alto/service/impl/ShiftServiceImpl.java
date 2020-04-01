package com.alto.service.impl;


import com.alto.model.*;
import com.alto.model.requests.PushMessageRequest;
import com.alto.model.requests.SessionsRequest;
import com.alto.model.requests.ShiftRequest;
import com.alto.model.response.ClientResponse;
import com.alto.model.response.GeoCodeResponse;
import com.alto.model.response.ShiftResponse;
import com.alto.model.response.TempResponse;
import com.alto.repository.AppUserRepository;
import com.alto.repository.ShiftRepository;
import com.alto.repository.UserPreferencesRepository;
import com.alto.service.ShiftService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.internal.Utilities;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import java.util.*;


@Service
public class ShiftServiceImpl implements ShiftService {


  @Autowired
  Environment env;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private ShiftRepository shiftRepository;
  @Autowired
  private AppUserRepository appUserRepository;
  @Autowired
  ResourceLoader resourceLoader;
  @Autowired
  UserPreferencesRepository userPreferencesRepository;

  //List<ShiftResponse>

  final static int BREAK_START = 1;
  final static int BREAK_END = 2;
  final static int SHIFT_END = 3;

//  @Scheduled(fixedRate = 2000)
//  public void scheduleTaskWithFixedRate() {
//    //logger.info("Fixed Rate Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()) );
//    System.out.println("Scheduled load");
//  }

  @Override
  public Shift findById(Long id) {
    return null;
  }

  public ResponseEntity addShift(ShiftRequest request){
    ShiftResponse started = null;
    Shift saveShift = new Shift();

    //todo externalize
    String getShiftUrl = "https://ctms.contingenttalentmanagement.com/CirrusConcept/clearConnect/2_0/index.cfm?action=getOrders&username=rsteele&password=altoApp1!&status=filled&tempId=$tempId&orderId=$orderId&resultType=json";
    getShiftUrl = getShiftUrl.replace("$tempId",request.getTempId().toString());
    getShiftUrl = getShiftUrl.replace("$orderId",request.getOrderId());
    try {

      RestTemplate restTemplate = new RestTemplateBuilder().build();

      try {

        String result = restTemplate.getForObject(getShiftUrl, String.class);
        result = result.replace("[","").replace("]","");

        System.out.println(result);

        Gson gson = new Gson();
        started = gson.fromJson(result, ShiftResponse.class);
        if(!checkGeoFence(request)){
          return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

      } catch (Exception e) {
        //LOGGER.error("Error getting Embed URL and Token", e);
      }

      //save to repo
      saveShift.setOrderid(request.getOrderId());
      saveShift.setTempid(request.getTempId().toString());
      saveShift.setUsername(request.getUsername());
      saveShift.setShiftStartTime(convertFromString(started.getShiftStartTime()));
      saveShift.setShiftEndTime(convertFromString(started.getShiftEndTime()));
      saveShift.setShiftStartTimeActual(new Timestamp(System.currentTimeMillis()));
      saveShift.setStatus(started.getStatus());
      saveShift.setClientId(started.getClientId());
      saveShift.setClientName(started.getClientName());
      saveShift.setShiftStartSignoff(request.getShiftSignoff());
      saveShift.setFloor(started.getFloor());
      saveShift.setShiftNumber(started.getShiftNumber());
      saveShift.setOrderCertification(started.getOrderCertification());
      saveShift.setOrderSpecialty(started.getOrderSpecialty());
      saveShift.setNote(started.getNote());
      saveShift.setClockInAddress(request.getClockedAddy());
      saveShift.setCheckinLat(request.getLat());
      saveShift.setCheckinLon(request.getLon());

      shiftRepository.saveAndFlush(saveShift);

    } catch(Exception e) {
      e.printStackTrace();
      //LOGGER.error("Error getting Embed URL and Token", e);
    }

    return new ResponseEntity(saveShift, HttpStatus.OK);
  }

  public List<ShiftResponse> getScheduled(String tempid){
    List<ShiftResponse> results = new ArrayList<>();

    //todo externalize
    String getShiftUrl = "https://ctms.contingenttalentmanagement.com/CirrusConcept/clearConnect/2_0/index.cfm?action=getOrders&username=rsteele&password=altoApp1!&status=filled&tempId=$tempId&status=filled&orderBy1=shiftStart&orderByDirection1=ASC&shiftStart="+ ZonedDateTime.now( ZoneOffset.UTC ).format( java.time.format.DateTimeFormatter.ISO_INSTANT )+"&resultType=json";
    getShiftUrl = getShiftUrl.replace("$tempId",tempid);
    //getShiftUrl = getShiftUrl.replace("$orderId",request.getOrderId());

      RestTemplate restTemplate = new RestTemplateBuilder().build();

      try {
        String result = restTemplate.getForObject(getShiftUrl, String.class);
        //result = result.replace("[","").replace("]","");


        Gson gson = new Gson(); // Or use new GsonBuilder().create();
        Type userListType = new TypeToken<ArrayList<ShiftResponse>>(){}.getType();

         results = gson.fromJson(result, userListType);
         if(results == null){
           results = new ArrayList<>();
         }

        //results = pruneResults(tempid, results);

      } catch (Exception e) {
        e.printStackTrace();
        //LOGGER.error("Error getting Embed URL and Token", e);
      }

    return results;
  }

  public List<ShiftResponse> getOpens(String tempid){
    List<ShiftResponse> resultsOpens = new ArrayList<>();

    //todo externalize
    String getOpensUrl = "https://ctms.contingenttalentmanagement.com/CirrusConcept/clearConnect/2_0/index.cfm?action=getOrders&username=rsteele&password=altoApp1!&status=open&status=open&orderBy1=shiftStart&orderByDirection1=ASC&shiftStart="+ ZonedDateTime.now( ZoneOffset.UTC ).format( java.time.format.DateTimeFormatter.ISO_INSTANT )+"&shiftEnd="+ ZonedDateTime.now( ZoneOffset.UTC ).plusDays(30).format( java.time.format.DateTimeFormatter.ISO_INSTANT )+"&resultType=json";
    getOpensUrl = getOpensUrl.replace("$tempId",tempid);
    //getShiftUrl = getShiftUrl.replace("$orderId",request.getOrderId());

    RestTemplate restTemplate = new RestTemplateBuilder().build();

    try {
      String resultOpens = restTemplate.getForObject(getOpensUrl, String.class);
      //result = result.replace("[","").replace("]","");


      Gson gson = new Gson(); // Or use new GsonBuilder().create();
      Type userListType = new TypeToken<ArrayList<ShiftResponse>>(){}.getType();

      resultsOpens = gson.fromJson(resultOpens, userListType);

      resultsOpens = pruneResults(tempid, resultsOpens);

    } catch (Exception e) {
      e.printStackTrace();
      //LOGGER.error("Error getting Embed URL and Token", e);
    }

    return resultsOpens;
  }

  public ResponseEntity updateShift(ShiftRequest request){
    ShiftResponse started = null;
    Shift updateShift = null;

    try {

      updateShift = shiftRepository.findByOrderid(request.getOrderId());
      if(updateShift == null){
        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
      }
      if(!checkGeoFence(request)){
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }

//todo commented break stuff
      //save to repo
//      if(request.getShiftstatuskey() == BREAK_START){
//        updateShift.setBreakStartTime(new Timestamp(System.currentTimeMillis()));
//      }else if(request.getShiftstatuskey() == BREAK_END){
//        updateShift.setBreakEndTime(new Timestamp(System.currentTimeMillis()));
//      }else if(request.getShiftstatuskey() == SHIFT_END){
        updateShift.setShiftEndTimeActual(new Timestamp(System.currentTimeMillis()));
        updateShift.setShiftEndSignoff(request.getShiftSignoff());
        updateShift.setClockoutAddress(request.getClockedAddy());
        updateShift.setCheckoutLat(request.getLat());
        updateShift.setCheckoutLon(request.getLon());
     // }
      shiftRepository.saveAndFlush(updateShift);


    } catch(Exception e) {
      e.printStackTrace();
      //LOGGER.error("Error getting Embed URL and Token", e);
    }

    return new ResponseEntity(updateShift, HttpStatus.OK);
  }

  private Timestamp convertFromString(String input){
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
      Date parsedDate = dateFormat.parse(input);
      return new java.sql.Timestamp(parsedDate.getTime());
    } catch(Exception e) { //this generic but you can control another types of exception
      // look the origin of excption
      e.printStackTrace();
    }
    return null;
  }

  public Shift getShift(String orderid){
    return shiftRepository.findByOrderid(orderid);
  }

  private List<ShiftResponse> pruneResults(String tempid, List<ShiftResponse> openShifts){

    UserPreferences prefs =  userPreferencesRepository.findByTempid(Long.parseLong(tempid));
    if(prefs == null) return openShifts;

    List<ShiftResponse> results = new ArrayList<>();



    for(ShiftResponse shift : openShifts){
      DateTime dt = new DateTime( shift.getShiftStartTime() ) ;
      DateTimeFormatter fmt = DateTimeFormat.forPattern("EEE"); // use 'E' for short abbreviation (Mon, Tues, etc)
      String strEnglish = fmt.print(dt);

      switch(strEnglish)
      {
        case "Sun":
          if(prefs.getSunday()) results.add(shift);
          break;
        case "Mon":
          if(prefs.getMonday()) results.add(shift);
          break;
        case "Tue":
          if(prefs.getTuesday()) results.add(shift);
          break;
        case "Wed":
          if(prefs.getWednesday()) results.add(shift);
          break;
        case "Thu":
          if(prefs.getThursday()) results.add(shift);
          break;
        case "Fri":
          if(prefs.getFriday()) results.add(shift);
          break;
        case "Sat":
          if(prefs.getSaturday()) results.add(shift);
          break;
        default:
          results.add(shift);
      }
    }

    return results;
  }

  private Boolean checkGeoFence(ShiftRequest request){

    ClientResponse client = null;
    List<GeoCodeResponse> geoList = new ArrayList<>();

    String getClientUrl = "https://ctms.contingenttalentmanagement.com/CirrusConcept/clearConnect/2_0/index.cfm?action=getClients&username=rsteele&password=altoApp1!&clientIdIn="+request.getClientId()+"&resultType=json";
    String getCoordsURL = "https://us1.locationiq.com/v1/search.php?key=01564e14da0703&q=$searchstring&format=json";
    RestTemplate restTemplate = new RestTemplateBuilder().build();

      try {

        String result = restTemplate.getForObject(getClientUrl, String.class);
        result = result.replace("[","").replace("]","");

        System.out.println(result);

        Gson gson = new Gson(); // Or use new GsonBuilder().create();
        client = gson.fromJson(result, ClientResponse.class);

       // getCoordsURL = getCoordsURL.replace("$searchstring",client.getAddress());
        getCoordsURL = getCoordsURL.replace("$searchstring", client.getAddress() + " " +client.getCity()+ " " + client.getState());

        //1010 Taywood Rd
        String goeResp= restTemplate.getForObject(getCoordsURL, String.class);
        Type userListType = new TypeToken<ArrayList<GeoCodeResponse>>(){}.getType();

        geoList = gson.fromJson(goeResp, userListType);

        for(GeoCodeResponse geo : geoList){
          //Double dist = haversine(39.861742, -84.290875, Double.parseDouble(geo.getLat()), Double.parseDouble(geo.getLon()));
          Double dist = haversine(Double.parseDouble(request.getLat()), Double.parseDouble(request.getLon()), Double.parseDouble(geo.getLat()), Double.parseDouble(geo.getLon()));
          if(dist < 0.3){
            return true;
          }
        }



      } catch (Exception e) { //todo logger
        //LOGGER.error("Error getting Embed URL and Token", e);
      }

    return false;
  }

  public static final double R = 6372.8; // In kilometers
  public static double haversine(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    lat1 = Math.toRadians(lat1);
    lat2 = Math.toRadians(lat2);

    double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
    double c = 2 * Math.asin(Math.sqrt(a));
    return R * c;
  }

  public List<Sessions> sessionsData(SessionsRequest request){

    Timestamp fromTS1 = null;
    Timestamp fromTS2 = null;
    List<TempResponse>  tempHcs = null;
    List<Sessions> results = new ArrayList<>();
    List<Shift> shifts = new ArrayList<>();
    //todo externalize
    String getActiveTempsUrl = "https://ctms.contingenttalentmanagement.com/CirrusConcept/clearConnect/2_0/index.cfm?action=getTemps&username=rsteele&password=altoApp1!&statusIn=Active&resultType=json";

    try {

      SimpleDateFormat datetimeFormatter1 = new SimpleDateFormat("E MMM dd yyyy");
      Date lFromDate1 = datetimeFormatter1.parse(request.getStart());
      fromTS1 = new Timestamp(lFromDate1.getTime());

      SimpleDateFormat datetimeFormatter2 = new SimpleDateFormat("E MMM dd yyyy");
      Date lFromDate2 = datetimeFormatter2.parse(request.getEnd());
      fromTS2 = new Timestamp(lFromDate2.getTime());

      RestTemplate restTemplateTemp = new RestTemplateBuilder().build();
      String resultTemp = restTemplateTemp.getForObject(getActiveTempsUrl, String.class);

      Gson gson = new Gson(); // Or use new GsonBuilder().create();
      TempResponse[] mcArray = gson.fromJson(resultTemp, TempResponse[].class);
      tempHcs = new ArrayList<>(Arrays.asList(mcArray));

    }catch(Exception e){
      e.printStackTrace();
    }

    shifts = shiftRepository.findByDates(fromTS1, fromTS2);

    for(Shift s : shifts){

      TempResponse match = tempHcs.stream()
              .filter(temp -> s.getTempid().equals(temp.getTempId()))
              .findAny()
              .orElse(null);

      if(match != null) {

        Sessions sess = new Sessions();
        sess.setBreakEndTime(s.getBreakEndTime());
        sess.setBreakStartTime(s.getBreakStartTime());
        sess.setCheckinLat(s.getCheckinLat());
        sess.setCheckinLon(s.getCheckinLon());
        sess.setCheckoutLat(s.getCheckoutLat());
        sess.setCheckoutLon(s.getCheckoutLon());
        sess.setClientId(s.getClientId());
        sess.setClientName(s.getClientName());
        sess.setClockInAddress(s.getClockInAddress());
        sess.setClockoutAddress(s.getClockoutAddress());
        sess.setFloor(s.getFloor());
        sess.setOrderCertification(s.getOrderCertification());
        sess.setOrderid(s.getOrderid());
        sess.setOrderSpecialty(s.getOrderSpecialty());
        sess.setShiftEndSignoff(s.getShiftEndSignoff());
        sess.setShiftEndTime(s.getShiftEndTime());
        sess.setShiftEndTimeActual(s.getShiftEndTimeActual());
        sess.setShiftNumber(s.getShiftNumber());
        sess.setShiftStartSignoff(s.getShiftStartSignoff());
        sess.setShiftStartTime(s.getShiftStartTime());
        sess.setShiftStartTimeActual(s.getShiftStartTimeActual());
        sess.setStatus(s.getStatus());
        sess.setTempid(s.getTempid());
        sess.setUsername(s.getUsername());
        sess.setTempName(match.getFirstName() + " " + match.getLastName());

        results.add(sess);

      }
        //todo else log no matching temp found for shift
    }
    return results;
  }


  public void sendPushNotification(PushMessageRequest message){

    for(String tempid : message.getTemps()){
      AppUser user = appUserRepository.findByTempid(tempid);

      if(user == null) continue;

      if(user.getDevicetype().equalsIgnoreCase("Android") && user.getDevicetoken() != null && user.getDevicetoken().length() > 10){

        sendFMSNotigication(user.getDevicetoken(), message.getMsgBody());
        //sendAPNSNotification(user.getDevicetoken(), message.getMsgBody());
      }else if(user.getDevicetype().equalsIgnoreCase("iOS") && user.getDevicetoken() != null && user.getDevicetoken().length() > 10){

        sendAPNSNotification(user.getDevicetoken(), message.getMsgBody());
        //sendFMSNotigication(user.getDevicetoken(), message.getMsgBody());
      }
    }

  }

  private void sendFMSNotigication(String deviceToken, String messg) {

    try {
      String androidFcmKey = "AAAAiJJmHX4:APA91bGFT2PxR2V8tJZr0JN7PSKVXmCR9BRnhCAR5-bpWGbcAnDdgNla16CUvJvWiGDY8n57YLnOLTcsDVwGC9nYXkH3VGoUm3_vfPqxXENzOgi3JRQRjP_RfbP-_84QCKjwoUO5Lv_l";
      String androidFcmUrl = "https://fcm.googleapis.com/fcm/send";

      RestTemplate restTemplate = new RestTemplate();
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Authorization", "key=" + androidFcmKey);
      httpHeaders.set("Content-Type", "application/json");
      JSONObject msg = new JSONObject();
      JSONObject json = new JSONObject();

      msg.put("title", "New message from Alto!");
      msg.put("body", messg);
      //msg.put("notificationType", "Test");

      json.put("notification", msg);
      json.put("to", deviceToken);


      HttpEntity<String> httpEntity = new HttpEntity<String>(json.toString(), httpHeaders);
      String response = restTemplate.postForObject(androidFcmUrl, httpEntity, String.class);
      System.out.println(response);
    } catch (Exception e) {
      e.printStackTrace();
      //LOGGER.error("Error:", e);
    }
  }

  private void sendAPNSNotification(String deviceToken, String messg){

    try {

//// See documentation on defining a message payload.
//      Message message = Message.builder()
//              .putData("score", "850")
//              .putData("time", "2:45")
//              .setToken(deviceToken)
//              .build();
//
//// Send a message to the device corresponding to the provided
//// registration token.
//      String response = FirebaseMessaging.getInstance().send(message);
//// Response is a message ID string.
//      System.out.println("Successfully sent message: " + response);


      ApnsService service;

      ClassLoader classLoader = new ShiftServiceImpl().getClass().getClassLoader();

      File file = new File(classLoader.getResource("apns.p12").getFile());
      InputStream targetStream = new FileInputStream(file);

      service = APNS.newService().withCert(targetStream, "altoapp")
                .withProductionDestination().build();

      String payload = APNS.newPayload().customField("customData",messg)
              .alertBody("Message").build();
      service.push(Utilities.encodeHex(deviceToken.getBytes()), payload);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
