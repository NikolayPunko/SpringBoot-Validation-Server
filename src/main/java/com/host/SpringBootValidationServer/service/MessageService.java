package com.host.SpringBootValidationServer.service;

import com.host.SpringBootValidationServer.model.NsGrNmsg;
import com.host.SpringBootValidationServer.model.NsNmsg;
import com.host.SpringBootValidationServer.model.NsNnode;
import com.host.SpringBootValidationServer.model.NsNrule;
import com.host.SpringBootValidationServer.repositories.GRNMSGRepository;
import com.host.SpringBootValidationServer.repositories.NMSGRepository;
import com.host.SpringBootValidationServer.repositories.NNODERepository;
import com.host.SpringBootValidationServer.repositories.NRULERepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Slf4j
@Service
public class MessageService {

    public static Map<String, NsNmsg> NS_NMSG_MAP = new HashMap<>();
    public static Map<String, List<NsNnode>> NS_NNODE_MAP = new HashMap<>();
    public static Map<String, List<NsNrule>> NS_NRULE_MAP = new HashMap<>();
    public static Map<String, NsGrNmsg> NS_GRNMSG_MAP = new HashMap<>();


    private final NMSGRepository nmsgRepository;
    private final NNODERepository nnodeRepository;
    private final NRULERepository nruleRepository;
    private final GRNMSGRepository grnmsgRepository;

    @Autowired
    public MessageService(NMSGRepository nmsgRepository, NNODERepository nnodeRepository, NRULERepository nruleRepository, GRNMSGRepository grnmsgRepository) {
        this.nmsgRepository = nmsgRepository;
        this.nnodeRepository = nnodeRepository;
        this.nruleRepository = nruleRepository;
        this.grnmsgRepository = grnmsgRepository;
    }

    @PostConstruct
    private void postConstruct() {
        for (NsNmsg obj : nmsgRepository.findAll()) {
            NS_NMSG_MAP.put(obj.getMsgType().trim(), obj);
        }

        for (NsNnode obj : nnodeRepository.findAll()) {
            List<NsNnode> nnodeList = NS_NNODE_MAP.get(obj.getKnm().trim()) == null ? new ArrayList<>() : NS_NNODE_MAP.get(obj.getKnm().trim());
            nnodeList.add(obj);
            NS_NNODE_MAP.put(obj.getKnm().trim(), nnodeList);
        }

        for (NsNrule obj : nruleRepository.findAll()) {
            List<NsNrule> nruleList = NS_NRULE_MAP.get(obj.getKnm().trim()) == null ? new ArrayList<>() : NS_NRULE_MAP.get(obj.getKnm().trim());
            nruleList.add(obj);
            NS_NRULE_MAP.put(obj.getKnm().trim(), nruleList);
        }

        for (NsGrNmsg obj : grnmsgRepository.findAll()) {
            NS_GRNMSG_MAP.put(obj.getKgr().trim(), obj);
        }
    }

    public void checkSender(String sender,  List<String> errorList){
        if(!NS_GRNMSG_MAP.containsKey(sender)){
            errorList.add("Sender не прописан в правилах марштрутизации;");
            throw new RuntimeException("Sender не прописан в правилах марштрутизации;");
        }
    }

    public String findKnmMsg(String msgType) {
        return NS_NMSG_MAP.get(msgType).getKnm().trim();
    }

    public List<String> getListReceivers(String facility, String knmMsg, String sender) {

        List<String> receiverList = new ArrayList<>();

        boolean isValidMsg = false;

        for (NsNrule x : NS_NRULE_MAP.get(knmMsg)) {
            if (x.getFacility().trim().equalsIgnoreCase(facility)) {
                if (x.getSender().trim().equalsIgnoreCase(sender)) {
                    isValidMsg = true;
                }

                receiverList.add(x.getReceiver().trim());
            }
        }

        if (!isValidMsg) {
            throw new RuntimeException("Cообщение не прописано в правилах маршрутизации!");
        }

        return receiverList;
    }





}
