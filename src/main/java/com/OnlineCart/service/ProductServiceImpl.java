package com.OnlineCart.service;

import com.OnlineCart.model.Product;
import com.OnlineCart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{
@Autowired
private ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    @Override
    public Boolean deleteProduct(Integer id) {

        Product product = productRepository.findById(id).orElse(null);
        if(!ObjectUtils.isEmpty(product))
        {
            productRepository.delete(product);
            return true;
        }

        return false;
    }

    @Override
    public Product getProductById(Integer id) {

        Product product = productRepository.findById(id).orElse(null);
        return product;
    }
@Override
public Product updateProduct(Product product, MultipartFile image) throws IOException {

    Product dbProduct = getProductById(product.getId());
    if (ObjectUtils.isEmpty(dbProduct)) {
        return null;
    }

    // determine image name (ensure a non-empty safe filename)
    String imageName;
    if (image == null || image.isEmpty()) {
        imageName = dbProduct.getImage();
    } else {
        imageName = org.springframework.util.StringUtils.cleanPath(image.getOriginalFilename());
        if (imageName == null || imageName.isBlank()) {
            imageName = "img_" + System.currentTimeMillis();
        }
    }

    // update fields
    dbProduct.setTitle(product.getTitle());
    dbProduct.setCategory(product.getCategory());
    dbProduct.setDescription(product.getDescription());
    dbProduct.setPrice(product.getPrice());
    dbProduct.setStock(product.getStock());
    dbProduct.setImage(imageName);
    dbProduct.setIsActive(product.getIsActive());
    dbProduct.setDiscount(product.getDiscount());

    Double discount = product.getPrice()*(product.getDiscount()/100.0);

    Double discountPrice = product.getPrice()-discount;

    dbProduct.setDiscountPrice(discountPrice);
    // save updated product to DB
    Product updatedProduct = productRepository.save(dbProduct);

    // handle file save only when an actual file was uploaded
    if (image != null && !image.isEmpty()) {
        try {
            File staticImgDir = new ClassPathResource("static/img").getFile();
            Path uploadDir = Paths.get(staticImgDir.getAbsolutePath(), "product_img");

            // ensure upload dir exists
            Files.createDirectories(uploadDir);

            Path target = uploadDir.resolve(imageName);

           
            if (Files.exists(target) && Files.isDirectory(target)) {
                int dot = imageName.lastIndexOf('.');
                String base = dot > 0 ? imageName.substring(0, dot) : imageName;
                String ext = dot > 0 ? imageName.substring(dot) : "";
                String uniqueName = base + "_" + System.currentTimeMillis() + ext;
                imageName = uniqueName;
                target = uploadDir.resolve(imageName);

                // update DB with new unique filename
                updatedProduct.setImage(imageName);
                updatedProduct = productRepository.save(updatedProduct);
            }

            // copy uploaded file to resolved file target (replace existing regular file)
            try (var is = image.getInputStream()) {
                Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            // log and rethrow so controller can show an error if needed
            ex.printStackTrace();
            throw ex;
        }
    }

    return updatedProduct;
}

    @Override
    public List<Product> getAllActiveProducts(String category) {

        List<Product> products = null;
        if(ObjectUtils.isEmpty(category))
        {
            products = productRepository.findByIsActiveTrue();
        }
        else {
            products = productRepository.findByCategory(category);
        }

        return products;
    }

    @Override
    public List<Product> searchProduct(String ch) {
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
    }

    @Override
    public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
    }

    @Override
    public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category) {

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> pageProduct = null;

        if (ObjectUtils.isEmpty(category)) {
            pageProduct = productRepository.findByIsActiveTrue(pageable);
        } else {
            pageProduct = productRepository.findByCategory(pageable, category);
        }
        return pageProduct;
    }

    @Override
    public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch) {

        Page<Product> pageProduct = null;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        pageProduct = productRepository.findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch,
                ch, pageable);

//		if (ObjectUtils.isEmpty(category)) {
//			pageProduct = productRepository.findByIsActiveTrue(pageable);
//		} else {
//			pageProduct = productRepository.findByCategory(pageable, category);
//		}
        return pageProduct;
    }
}
