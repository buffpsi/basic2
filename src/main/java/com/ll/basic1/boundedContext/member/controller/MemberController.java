package com.ll.basic1.boundedContext.member.controller;

import com.ll.basic1.base.rsData.RsData;
import com.ll.basic1.boundedContext.member.entity.Member;
import com.ll.basic1.boundedContext.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;


@Controller
public class MemberController {
    private final MemberService memberService;

    // 생성자 주입
    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/member/login")
    @ResponseBody
    public RsData login(String username, String password, HttpServletResponse resp) {
        if (username == null || username.trim().length() == 0) {
            return RsData.of("F-3", "username(을)를 입력해주세요.");
        }

        if (password == null || password.trim().length() == 0) {
            return RsData.of("F-4", "password(을)를 입력해주세요.");
        }

        RsData rsData = memberService.tryLogin(username, password);

        if (rsData.isSuccess()) {
            long memberId = (long) rsData.getData();
            resp.addCookie(new Cookie("loginedMemberId", memberId + ""));
        }

        return rsData;
    }

    @GetMapping("/member/logout")
    @ResponseBody
    public RsData logout(HttpServletRequest req, HttpServletResponse resp) {
        Rq rq = new Rq(req, resp);
        rq.removeCookie("loginedMemberId");

        return RsData.of("S-1", "로그아웃 되었습니다.");
    }

    @GetMapping("/member/me")
    @ResponseBody
    public RsData showMe(HttpServletRequest req) {
        Rq rq = new Rq(req);
        long loginedMemberId = rq.getLoginedMemberId();
        if (loginedMemberId == 0) {
            return RsData.of("F-5", "로그인 후 이용해주세요.");
        }
        Member member = memberService.findById(loginedMemberId);
        return RsData.of("S-1", "당신의 username은"+member.getUsername()+"입니다.");
    }
}

class Rq {
    private HttpServletRequest req;

    public Rq(HttpServletRequest req) {
        this.req = req;
    }

    private HttpServletResponse resp;

    public Rq(HttpServletRequest req, HttpServletResponse resp) {
        this.req = req;
        this.resp = resp;
    }

    public void removeCookie(String name) {
        if (req.getCookies() != null) {
            Arrays.stream(req.getCookies())
                    .filter(cookie -> cookie.getName().equals(name))
                    .forEach(cookie -> {
                        cookie.setMaxAge(0);
                        resp.addCookie(cookie);
                    });
        }
    }

    public long getLoginedMemberId() {

        if (req.getCookies() != null) {
            return Arrays.stream(req.getCookies())
                    .filter(cookie -> cookie.getName().equals("loginedMemberId"))
                    .map(Cookie::getValue)
                    .mapToLong(Long::parseLong)
                    .findFirst()
                    .orElse(0);
        }
        return 0;
    }
}
