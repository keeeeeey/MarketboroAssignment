package com.example.marketboro.controller;

import com.example.marketboro.dto.Success;
import com.example.marketboro.dto.request.CartRequestDto;
import com.example.marketboro.dto.request.CartRequestDto.AddOrUpdateCartDto;
import com.example.marketboro.dto.request.CartRequestDto.DeleteCartDto;
import com.example.marketboro.security.UserDetailsImpl;
import com.example.marketboro.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/api/cart")
    public ResponseEntity<Success> addCart(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestBody AddOrUpdateCartDto requestDto) {
        if (userDetails != null) {
            return new ResponseEntity<>(new Success("장바구니 담기",
                    cartService.addCart(userDetails.getUser().getId(), requestDto)), HttpStatus.OK);
        }
        throw new RuntimeException("로그인 후 이용 가능합니다.");
    }

    @PatchMapping("/api/cart")
    public ResponseEntity<Success> updateCart(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @RequestBody AddOrUpdateCartDto requestDto) {
        if (userDetails != null) {
            return new ResponseEntity<>(new Success("장바구니 수정",
                    cartService.updateCart(userDetails.getUser().getId(), requestDto)), HttpStatus.OK);
        }
        throw new RuntimeException("로그인 후 이용 가능합니다.");
    }

    @DeleteMapping("/api/cart")
    public ResponseEntity<Success> deleteCart(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @RequestBody DeleteCartDto requestDto) {
        if (userDetails != null) {
            cartService.deleteCart(userDetails.getUser().getId(), requestDto);
            return new ResponseEntity<>(new Success("장바구니 수정", ""), HttpStatus.OK);
        }
        throw new RuntimeException("로그인 후 이용 가능합니다.");
    }
}
