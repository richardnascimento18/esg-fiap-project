package com.ecocity.esg.adapter.out.persistence.mongodb.repository;

import com.ecocity.esg.adapter.out.persistence.mongodb.document.WasteCollectionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WasteCollectionMongoRepository extends MongoRepository<WasteCollectionDocument, String> {
}
