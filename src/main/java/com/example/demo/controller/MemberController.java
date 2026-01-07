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
    public ApiResponse<Member> createMember(
            @RequestParam String phone,
            @RequestParam(required = false) String name) {
        try {
            Member member = memberService.createMember(phone, name);
            return ApiResponse.success("会员创建成功", member);
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