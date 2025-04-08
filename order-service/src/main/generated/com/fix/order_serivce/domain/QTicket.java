package com.fix.order_serivce.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTicket is a Querydsl query type for Ticket
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTicket extends EntityPathBase<Ticket> {

    private static final long serialVersionUID = -115343968L;

    public static final QTicket ticket = new QTicket("ticket");

    public final com.fix.common_service.entity.QBasic _super = new com.fix.common_service.entity.QBasic(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    //inherited
    public final NumberPath<Long> deletedBy = _super.deletedBy;

    //inherited
    public final BooleanPath isDeleted = _super.isDeleted;

    public final ComparablePath<java.util.UUID> orderId = createComparable("orderId", java.util.UUID.class);

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final ComparablePath<java.util.UUID> seatId = createComparable("seatId", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> ticketId = createComparable("ticketId", java.util.UUID.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public QTicket(String variable) {
        super(Ticket.class, forVariable(variable));
    }

    public QTicket(Path<? extends Ticket> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTicket(PathMetadata metadata) {
        super(Ticket.class, metadata);
    }

}

