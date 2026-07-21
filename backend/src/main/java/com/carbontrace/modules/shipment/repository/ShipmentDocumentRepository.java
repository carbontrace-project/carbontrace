package com.carbontrace.modules.shipment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carbontrace.modules.shipment.entity.ShipmentDocument;

/**
 * Data access for {@code shipment_documents}.
 */
@Repository
public interface ShipmentDocumentRepository extends JpaRepository<ShipmentDocument, Long> {

    /**
     * The documents belonging to a shipment.
     *
     * <p>Returns a {@link List} even though Section 9 allows exactly one document
     * per shipment in the MVP. The relationship is one-to-many in Section 7, and
     * nothing in the schema enforces the single-document rule — an
     * {@code Optional} return would throw
     * {@code IncorrectResultSizeDataAccessException} if a second row ever
     * appeared, turning a data problem into a 500 at read time. A list reports
     * what is actually there and lets the service (STEP A017) enforce the rule
     * where the rule lives.
     */
    List<ShipmentDocument> findByShipmentId(Long shipmentId);
}
