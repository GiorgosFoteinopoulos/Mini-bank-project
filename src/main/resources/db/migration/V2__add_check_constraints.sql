alter table account
    add constraint chk_account_withdrawn_today_non_negative check (withdrawn_today >= 0);

alter table account_transaction
    add constraint chk_transaction_amount_non_negative check (amount >= 0);