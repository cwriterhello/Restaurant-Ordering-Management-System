package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Member;
import com.example.demo.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "*")
public class MemberController {
    @Autowired
    private MemberService memberService;
    
    @GetMapping("/phone/{phone}")
    public ApiResponse<Member> getMemberByPhone(@PathVariable String phone) {
        try {
            Optional<Member> member = memberService.getMemberByPhone(phone);
            if (member.isPresent()) {
                return ApiResponse.success(member.get());
            } else {
                return ApiResponse.error(404, "会员不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping
    public ApiResponse<List<Member>> getAllMembers() {
        try {
            List<Member> members = memberService.getAllMembers();
            return ApiResponse.success(members);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<Member> createMember(@RequestBody Member member) {
        try {
            // 验证必填字段
            if (member.getPhone() == null || member.getPhone().trim().isEmpty()) {
                return ApiResponse.error("手机号不能为空");
            }
            
            // 设置默认值
            if (member.getLevel() == null || member.getLevel().trim().isEmpty()) {
                member.setLevel("NORMAL");
            }
            if (member.getDiscount() == null) {
                member.setDiscount(new java.math.BigDecimal("1.00"));
            }
            if (member.getName() == null) {
                member.setName("");
            }
            
            Member createdMember = memberService.createMember(
                member.getPhone(), 
                member.getName()
            );
            
            // 如果传入了等级和折扣，更新这些信息
            if (!"NORMAL".equals(member.getLevel()) || member.getDiscount().compareTo(new java.math.BigDecimal("1.00")) != 0) {
                createdMember.setLevel(member.getLevel());
                createdMember.setDiscount(member.getDiscount());
                createdMember = memberService.updateMember(createdMember);
            }
            
            return ApiResponse.success("会员创建成功", createdMember);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Member> updateMember(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) java.math.BigDecimal discount) {
        try {
            Optional<Member> memberOpt = memberService.getMemberById(id);
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                if (name != null) member.setName(name);
                if (level != null) member.setLevel(level);
                if (discount != null) member.setDiscount(discount);
                Member updatedMember = memberService.updateMember(member);
                return ApiResponse.success("会员信息更新成功", updatedMember);
            } else {
                return ApiResponse.error(404, "会员不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}