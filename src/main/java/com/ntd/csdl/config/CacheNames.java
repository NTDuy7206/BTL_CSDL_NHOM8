package com.ntd.csdl.config;

public final class CacheNames {

    private CacheNames() {
    }

    // ROOM
    public static final String ROOMS = "rooms";
    public static final String ROOM_BY_ID = "roomById";
    public static final String ROOMS_BY_STATUS = "roomsByStatus";
    public static final String ROOMS_BY_TYPE = "roomsByType";
    public static final String ROOMS_BY_MAX_PRICE = "roomsByMaxPrice";

    // TENANT
    public static final String TENANTS = "tenants";
    public static final String TENANT_BY_ID = "tenantById";
    public static final String TENANTS_BY_NAME = "tenantsByName";

    // RULE
    public static final String RULES = "rules";
    public static final String RULE_BY_ID = "ruleById";

    // INVOICE
    public static final String INVOICES = "invoices";
    public static final String INVOICE_BY_ID = "invoiceById";
    public static final String INVOICES_BY_CONTRACT = "invoicesByContract";
    public static final String TOTAL_REVENUE = "totalRevenue";
}