package com.alto.service;

import com.alto.model.Shift;
import com.alto.model.requests.ShiftRequest;
import com.alto.model.*;
import com.alto.model.requests.PushMessageRequest;
import com.alto.model.requests.SessionsRequest;
import com.alto.model.response.ShiftResponse;

import java.util.List;


public interface ShiftService {

  Shift findById(Long id);
  Shift addShift(ShiftRequest request);
  Shift updateShift(ShiftRequest request);
  List<Sessions> sessionsData(SessionsRequest request);
  Shift getShift(String orderid);
  List<ShiftResponse>getScheduled(String tempid);
  List<ShiftResponse>getOpens(String tempid);
  void sendPushNotification(PushMessageRequest message);

}
