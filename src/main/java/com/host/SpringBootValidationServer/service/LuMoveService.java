package com.host.SpringBootValidationServer.service;


import com.host.SpringBootValidationServer.model.LuMove;
import com.host.SpringBootValidationServer.repositories.LuMoveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LuMoveService {

//    private final LuMoveRepository luMoveRepository;
//
//    @Autowired
//    public LuMoveService(LuMoveRepository luMoveRepository) {
//        this.luMoveRepository = luMoveRepository;
//    }

    @Transactional
    public void save(LuMove luMove) {
//        luMoveRepository.save(luMove);
    }

    public void saveLuMoveList(List<LuMove> luMoveList) {
        luMoveList.forEach(this::save);
    }

}
