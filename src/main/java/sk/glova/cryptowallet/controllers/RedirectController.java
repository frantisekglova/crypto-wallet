package sk.glova.cryptowallet.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RedirectController {

    @GetMapping
    public RedirectView redirectWithUsingRedirectView() {
        return new RedirectView("/swagger-ui.html");
    }

}
