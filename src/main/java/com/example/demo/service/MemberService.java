package com.example.demo.service;

import com.example.demo.entity.Member;
import com.example.demo.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {
    @Autowired
    private MemberMapper memberMapper;
    
    public Optional<Member> getMemberByPhone(String phone) {
        Member member = memberMapper.findByPhone(phone);
        return Optional.ofNullable(member);
    }
    
    public Optional<Member> getMemberById(Long id) {
        Member member = memberMapper.selectById(id);
        return Optional.ofNullable(member);
    }
    
    public List<Member> getAllMembers() {
        return memberMapper.selectList(null);
    }
    
    public Member createMember(String phone, String name) {
        if (memberMapper.findByPhone(phone) != null) {
            throw new RuntimeException("该手机号已注册");
        }
        
        Member member = new Member();
        member.setPhone(phone);
        member.setName(name);
        member.setLevel("NORMAL");
        member.setDiscount(new java.math.BigDecimal("1.00"));
        
        memberMapper.insert(member);
        return member;
    }
    
    public Member updateMember(Member member) {
        memberMapper.updateById(member);
        return member;
    }
}