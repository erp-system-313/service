-- Adds the invoice -> sales order column expected by Invoice.java / InvoiceService.java.
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS sales_order_id BIGINT;

DO $$
BEGIN
    ALTER TABLE invoices
        ADD CONSTRAINT fk_invoices_sales_order
        FOREIGN KEY (sales_order_id)
        REFERENCES sales_orders(id);
EXCEPTION
    WHEN duplicate_object THEN
        RAISE NOTICE 'constraint fk_invoices_sales_order already exists';
END $$;

CREATE INDEX IF NOT EXISTS idx_invoices_sales_order_id ON invoices(sales_order_id);
