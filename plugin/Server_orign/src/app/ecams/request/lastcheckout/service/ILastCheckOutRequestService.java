package app.ecams.request.lastcheckout.service;

import java.util.HashMap;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;

public interface ILastCheckOutRequestService {
	ReturnMsg select_lastver(EcamsMessage ecamsmsg);
}
