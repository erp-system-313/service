-- V51__fix_payments_table_schema.sql
-- Align payments table with the new Payment entity (Move-based payment system)

-- 1. Add new columns required by the Payment entity
ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_type VARCHAR(10);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS partner_type VARCHAR(10);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS currency_id BIGINT;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS partner_id BIGINT;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS partner_name VARCHAR(255);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_method_line_id BIGINT REFERENCES payment_method_lines(id);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS move_id BIGINT REFERENCES moves(id);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS is_reconciled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS is_internal_transfer BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(100);

-- 2. Backfill payment_type from existing data (default to INBOUND for existing payments)
UPDATE payments SET payment_type = 'INBOUND' WHERE payment_type IS NULL;

-- 3. Make invoice_id nullable (no longer required in new design)
ALTER TABLE payments ALTER COLUMN invoice_id DROP NOT NULL;

-- 4. Drop old columns that are no longer in the entity
-- (Keep them for now to avoid data loss; they'll be ignored by Hibernate)
-- ALTER TABLE payments DROP COLUMN IF EXISTS payment_method;
-- ALTER TABLE payments DROP COLUMN IF EXISTS reference;
-- ALTER TABLE payments DROP COLUMN IF EXISTS notes;

-- 5. Add constraint for payment_type
ALTER TABLE payments ADD CONSTRAINT chk_payments_payment_type
    CHECK (payment_type IN ('INBOUND', 'OUTBOUND'));

-- 6. Make payment_type NOT NULL after backfill
ALTER TABLE payments ALTER COLUMN payment_type SET NOT NULL;

-- 7. Create indexes for new foreign keys
CREATE INDEX IF NOT EXISTS idx_payments_move_id ON payments(move_id);
CREATE INDEX IF NOT EXISTS idx_payments_partner_id ON payments(partner_id);
CREATE INDEX IF NOT EXISTS idx_payments_payment_method_line_id ON payments(payment_method_line_id);
