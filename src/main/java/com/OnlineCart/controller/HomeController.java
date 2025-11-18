package com.OnlineCart.controller;

import com.OnlineCart.Utils.CommonUtil;
import com.OnlineCart.model.Category;
import com.OnlineCart.model.Product;
import com.OnlineCart.model.UserDatas;
import com.OnlineCart.service.CartService;
import com.OnlineCart.service.CategoryService;
import com.OnlineCart.service.ProductService;
import com.OnlineCart.service.UserDetailsService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;


    @ModelAttribute
    public void getUserDetails(Principal principal,Model model)
    {
        if(principal != null)
        {
           String email = principal.getName();
          UserDatas userDatas = userDetailsService.getUserByEmail(email);

          model.addAttribute("user",userDatas);

          Integer countCart = cartService.getCountCart(userDatas.getId());
          model.addAttribute("countCart",countCart);

        }

        List<Category> getAllActiveCategories = categoryService.getAllActiveCategory();
        model.addAttribute("categorys",getAllActiveCategories);

    }

    @GetMapping("/")
    public String index(Model m){

        List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream()
                .sorted((c1, c2) -> c2.getId().compareTo(c1.getId())).limit(6).toList();
        List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId())).limit(8).toList();
        m.addAttribute("category", allActiveCategory);
        m.addAttribute("products", allActiveProducts);

        return "index";
    }

    @GetMapping("/signin")
    public String loginPage()
    {
        return "login";
    }

    @GetMapping("/register")
    public String register()
    {
        return "register";
    }

    @GetMapping("/products")
    public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category,
                           @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "9") Integer pageSize,
                           @RequestParam(defaultValue = "") String ch)
    {
        List<Category> categories = categoryService.getAllActiveCategory();
       // List<Product> products = productService.getAllActiveProducts(category);
//        model.addAttribute("products",products);
          m.addAttribute("categories",categories);

          m.addAttribute("paramValue",category);

        Page<Product> page = null;
        if (StringUtils.isEmpty(ch)) {
            page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
        }else {
            page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
        }

        List<Product> products = page.getContent();
        m.addAttribute("products", products);
        m.addAttribute("paramValue", category);
        m.addAttribute("productsSize", products.size());

        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());


        return "products";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable int id,Model model){
      Product productById = productService.getProductById(id);
      model.addAttribute("product",productById);
        return "viewproducts";
    }

    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute UserDatas user,
                           @RequestParam("img") MultipartFile file, RedirectAttributes session)
            throws IOException
    {
        Boolean existsEmail = userDetailsService.existsEmail(user.getEmail());

        if (existsEmail) {
            session.addFlashAttribute("errorMsg", "Email already exist");
        } else {
            String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
            user.setProfileImage(imageName);
            UserDatas saveUser = userDetailsService.saveUser(user);

            if (!ObjectUtils.isEmpty(saveUser)) {
                if (!file.isEmpty()) {
                    File saveFile = new ClassPathResource("static/img").getFile();

                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
                            + file.getOriginalFilename());

//					System.out.println(path);
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                }
                session.addFlashAttribute("successMsg", "Register successfully");
            } else {
                session.addFlashAttribute("errorMsg", "something wrong on server");
            }
        }

        return "redirect:/register";
    }

    @GetMapping("/search")
    public String searchProduct(@RequestParam String ch, Model m) {
        List<Product> searchProducts = productService.searchProduct(ch);
        m.addAttribute("products", searchProducts);
        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("categories", categories);
        return "products";

    }

    @GetMapping("/forgot-password")
    public String showForgotPassword()
    {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                        Model model,
                                        RedirectAttributes session,
                                        HttpServletRequest request
                                            ) throws MessagingException, UnsupportedEncodingException {
        UserDatas user = userDetailsService.getUserByEmail(email);

        if(ObjectUtils.isEmpty(user))
        {
            session.addFlashAttribute("errorMsg","invalid email");
        }
        else{
           String reset_token = UUID.randomUUID().toString();
           userDetailsService.updateUserRestToken(email,reset_token);

          String url = CommonUtil.generateUrl(request) + "/reset-password?token="+reset_token;

           Boolean sendMail = commonUtil.sendMail(url,email);

           if(sendMail)
           {
               session.addFlashAttribute("successMsg","Check your email.. Password reset link is given");
           }
           else {
               session.addFlashAttribute("errorMsg","Something wrong on server ! Email not send");
           }

        }
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token,RedirectAttributes session,Model model)
    {

       UserDatas userByToken = userDetailsService.getUserByToken(token);

       if(ObjectUtils.isEmpty(userByToken))
       {
           model.addAttribute("msg","Your link is invalid or expired");
           return "error";
       }
       model.addAttribute("token",token);
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,RedirectAttributes session,Model model,@RequestParam String password)
    {

        UserDatas userByToken = userDetailsService.getUserByToken(token);
        if(ObjectUtils.isEmpty(userByToken))
        {
            model.addAttribute("errorMsg","Your link is invalid or expired");
            return "error";
        }
        else{
            userByToken.setPassword(passwordEncoder.encode(password));
            userByToken.setResetToken(null);
            userDetailsService.updateUser(userByToken);
            session.addFlashAttribute("successMsg","Password Change Successfully");
            model.addAttribute("msg","Password Change Successfully");
            return "error";
        }

    }




}
