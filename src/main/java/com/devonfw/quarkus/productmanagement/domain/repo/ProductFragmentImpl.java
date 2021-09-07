package com.devonfw.quarkus.productmanagement.domain.repo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.devonfw.quarkus.productmanagement.domain.model.ProductEntity;
import com.devonfw.quarkus.productmanagement.domain.model.ProductEntity_;
import com.devonfw.quarkus.productmanagement.domain.model.QProductEntity;
import com.devonfw.quarkus.productmanagement.service.v1.model.ProductSearchCriteriaDto;
import com.querydsl.jpa.impl.JPAQuery;

public class ProductFragmentImpl implements ProductFragment {

  @Inject
  EntityManager em;

  @Override
  public Page<ProductEntity> findAllCriteriaApi(ProductSearchCriteriaDto dto) {

    CriteriaQuery<ProductEntity> cq = this.em.getCriteriaBuilder().createQuery(ProductEntity.class);
    Root<ProductEntity> root = cq.from(ProductEntity.class);
    List<Predicate> predicates = new ArrayList<>();
    CriteriaBuilder cb = this.em.getCriteriaBuilder();
    if (dto.getTitle() != null && !dto.getTitle().isEmpty()) {
      predicates.add(cb.like(root.get(ProductEntity_.TITLE), dto.getTitle()));
    }

    if (dto.getPrice() != null) {
      predicates.add(cb.gt(root.get(ProductEntity_.PRICE), dto.getPrice()));
    } else if (dto.getPriceMin() != null || dto.getPriceMax() != null) {
      if (dto.getPriceMin() != null) {
        predicates.add(cb.gt(root.get(ProductEntity_.PRICE), dto.getPriceMin()));
      }
      if (dto.getPriceMax() != null) {
        predicates.add(cb.lt(root.get(ProductEntity_.PRICE), dto.getPriceMax()));
      }
    }

    if (!predicates.isEmpty())

    {
      cq.where(predicates.toArray(new Predicate[0]));
    }

    // Order by title
    cq.orderBy(cb.desc(root.get(ProductEntity_.TITLE)));

    TypedQuery<ProductEntity> Products = this.em.createQuery(cq).setFirstResult(dto.getPageNumber() * dto.getPageSize())
        .setMaxResults(dto.getPageSize());
    return new PageImpl<ProductEntity>(Products.getResultList(), PageRequest.of(dto.getPageNumber(), dto.getPageSize()),
        Products.getResultList().size());
  }

  @Override
  public Page<ProductEntity> findAllQueryDsl(ProductSearchCriteriaDto dto) {

    QProductEntity Product = QProductEntity.productEntity;
    JPAQuery<ProductEntity> query = new JPAQuery<ProductEntity>(this.em);
    query.from(Product);
    if (dto.getTitle() != null && !dto.getTitle().isEmpty()) {
      query.where(Product.title.eq(dto.getTitle()));
    }

    if (dto.getPrice() != null) {
      query.where(Product.price.eq(dto.getPrice()));
    } else if (dto.getPriceMin() != null || dto.getPriceMax() != null) {
      if (dto.getPriceMin() != null) {
        query.where(Product.price.gt(dto.getPriceMin()));
      }
      if (dto.getPriceMax() != null) {
        query.where(Product.price.lt(dto.getPriceMax()));
      }
    }

    // Order by title
    query.orderBy(Product.title.desc());

    List<ProductEntity> Products = query.limit(dto.getPageSize()).offset(dto.getPageNumber() * dto.getPageSize())
        .fetch();
    return new PageImpl<>(Products, PageRequest.of(dto.getPageNumber(), dto.getPageSize()), Products.size());
  }

  @Override
  public Page<ProductEntity> findByTitleQuery(ProductSearchCriteriaDto dto) {

    Query query = this.em.createQuery("select a from ProductEntity a where a.title = :title");
    query.setParameter("title", dto.getTitle());
    List<ProductEntity> Products = query.getResultList();
    return new PageImpl<>(Products, PageRequest.of(dto.getPageNumber(), dto.getPageSize()), Products.size());
  }

  @Override
  public Page<ProductEntity> findByTitleNativeQuery(ProductSearchCriteriaDto dto) {

    Query query = this.em.createNativeQuery("select * from ProductEntity where title = :title", ProductEntity.class);
    query.setParameter("title", dto.getTitle());
    List<ProductEntity> Products = query.getResultList();
    return new PageImpl<>(Products, PageRequest.of(dto.getPageNumber(), dto.getPageSize()), Products.size());
  }

}
