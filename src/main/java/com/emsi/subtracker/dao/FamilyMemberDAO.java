package com.emsi.subtracker.dao;

import com.emsi.subtracker.models.FamilyMember;
import java.util.List;


public interface FamilyMemberDAO {

    FamilyMember save(FamilyMember member);

    FamilyMember findById(int id);

    List<FamilyMember> findByUserId(int userId);


    FamilyMember findByUsername(String username);

    boolean update(FamilyMember member);

    boolean delete(int id);

    int countByUserId(int userId);
}
