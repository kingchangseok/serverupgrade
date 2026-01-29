package app.ecams.request.checkoutcnl.service;

import java.util.HashMap;


import app.ecams.request.service.IRequestService;

public interface ICheckOutCnlRequestService extends IRequestService {
	String selectCmr1010_acptno(HashMap param);
}