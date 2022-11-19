package com.example.ebookmarket.app.post.controller;

import com.example.ebookmarket.app.member.entity.Member;
import com.example.ebookmarket.app.post.dto.PostFormDto;
import com.example.ebookmarket.app.post.entity.Post;
import com.example.ebookmarket.app.post.exception.ActorCanNotModifyException;
import com.example.ebookmarket.app.post.service.PostService;
import com.example.ebookmarket.app.security.dto.MemberContext;
import com.example.ebookmarket.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/write")
    public String writeForm() {

        return "post/write";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/write")
    public String write(@Valid PostFormDto postFormDto, @AuthenticationPrincipal MemberContext member) {

        Post post = postService.write(postFormDto, member.getMember());

        String msg = Util.url.encode("글이 작성되었습니다.");

        return "redirect:/post/%d?msg=%s".formatted(post.getId(), msg);

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public String detailPage(@PathVariable Long id, @AuthenticationPrincipal MemberContext memberContext, Model model) {

        Post post = postService.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 id의 글은 존재하지 않습니다."));

        Member member = memberContext.getMember();

        if (postService.actorCanHandle(member, post) == false) {
            throw new ActorCanNotModifyException();
        }

        model.addAttribute("member", member);
        model.addAttribute("post", post);

        return "post/detail";
    }

}