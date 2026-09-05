## Phase 1
# Gym Management — Business Rules

## 1. Project Scope — Phase 1

### Phase 1 covers:
- User authentication and authorization
- Member management
- Membership management
- Membership pricing
- Payment management
- Cash payments only

### Explicitly deferred to Phase 2:
- Personal training
- Workout plans
- Membership freeze/pause
- Discounts as a formal model
- Refunds/reversals
- Payment editing/deletion
- Membership cancellation/replacement
- Other payment modes

---

## 2. Users & Authorization

There are two roles:
- **ADMIN**
- **STAFF**

### Authentication
- Login uses email + password.
- Passwords are stored using BCrypt.
- Authentication uses JWT.
- API is stateless.
- Invalid credentials return **401**.
- Unauthenticated requests return **401**.
- Authenticated users without sufficient permission return **403**.

### Authorization

| Operation | ADMIN | STAFF |
|---|---|---|
| Create member | ✅ | ✅ |
| View member | ✅ | ✅ |
| Update member | ✅ | ✅ |
| Activate/deactivate member | ✅ | ❌ |
| Create membership | ✅ | ✅ |
| View membership | ✅ | ✅ |
| Activate/deactivate membership | ✅ | ❌ |
| Record payment | ✅ | ✅ |
| View payments | ✅ | ✅ |
| Manage standard pricing | ✅ | ❌ |
| Override membership price | ✅ | ❌ |

---

## 3. Member Rules

A member contains:
- id
- name
- email
- phoneNumber
- dateOfBirth
- gender
- address
- joiningDate
- active
- createdAt
- updatedAt

### Name
- Required.
- Maximum 100 characters.

### Email
- Required.
- Must be valid email format.
- Maximum 100 characters.
- Must be unique.

### Phone
- Required.
- Exactly 10 digits.
- Indian phone-number format.
- Must be unique.

### Date of Birth
- Optional.
- If provided:
  - Cannot be after joining date.
  - Member must be at least 6 years old on the joining date.

**Important:** Age is calculated against the joining date, not today's date.

### Gender
- Optional.
- Maximum 20 characters.

### Address
- Optional.
- Maximum 255 characters.

### Joining Date
- Required.
- Cannot be in the past.
- Today or a future date is allowed.

### Member activation
- New members are: `active = true`
- ADMIN can activate/deactivate members.
- Member activation state is independent from membership state.

---

## 4. Member vs Membership State

This distinction is important.

### Member state
- ACTIVE
- INACTIVE

This represents whether the person is currently eligible to use the gym.

### Membership state
- UPCOMING
- ACTIVE
- INACTIVE
- EXPIRED

This represents the lifecycle of a particular membership.

They are intentionally independent.

**Example:**
- Member → INACTIVE
- Membership → ACTIVE

The membership is still ACTIVE, but the member cannot effectively use the gym while the member itself is inactive.

---

## 5. Member Deactivation

When ADMIN deactivates a member:
- `Member.active = false`
- We do not modify any memberships.

**Example:**
- Member → INACTIVE
- Current membership → ACTIVE
- Upcoming membership → UPCOMING
- Old membership → EXPIRED

Everything remains unchanged. There is:
- no membership deactivation
- no date extension
- no cancellation
- no freeze
- no automatic modification

If the member is later reactivated:
- Member → ACTIVE
- Their memberships remain exactly as they were:
  - ACTIVE membership → usable again
  - INACTIVE membership → remains INACTIVE
  - UPCOMING membership → remains UPCOMING
  - EXPIRED membership → remains EXPIRED

---

## 6. Membership Model

Membership contains:
- id
- member
- membershipType
- startDate
- endDate
- amount
- active
- createdAt
- updatedAt

### Membership types:
- MONTHLY
- QUARTERLY
- HALF_YEARLY
- YEARLY

---

## 7. Membership Creation

A member must:
- Exist.
- Be active.
- Have no outstanding balance across previous memberships.
- Not already have an upcoming membership.
- Have a valid start date.

### Start Date
- Cannot be in the past.
- Must be on or after the member's joining date.
- Can be today or future.

**Example:** Member joining date = September 1
- Membership start: September 1 → ✅
- Membership start: September 5 → ✅
- Membership start: August 31 → ❌

---

## 8. Membership Start Date vs Payment Date

These are deliberately independent.

**Example:**
- Payment date: August 25
- Membership start: September 1

This is valid. The member can pay before their membership starts. Payment date does not determine membership start date.

---

## 9. Membership End Date

The client does not provide the end date. Backend calculates it.

### Rules:
- **MONTHLY**: startDate + 1 month - 1 day
- **QUARTERLY**: startDate + 3 months - 1 day
- **HALF_YEARLY**: startDate + 6 months - 1 day
- **YEARLY**: startDate + 1 year - 1 day

**Example:** Start: January 1, Monthly → End: January 31

---

## 10. Membership Overlap

Membership periods cannot overlap. This applies to all existing memberships, including inactive ones.

**Example:**
- Existing: January 1 → January 31
- New: January 20 → February 19
- ❌ Rejected.

A new membership must start after the latest existing membership's end date. This also applies when the previous membership has expired.

---

## 11. Upcoming Membership

Only one upcoming membership is allowed per member.

**Example:**
- Current membership: Aug 1 → Aug 31
- Upcoming: Sep 1 → Sep 30 → ✅ Allowed.

Trying to create another: Oct 1 → Oct 31 → ❌ Rejected while the September membership is still upcoming.

There is no replacement/cancellation mechanism in Phase 1.

---

## 12. Advance Purchase

Maximum advance purchase: **30 days before the membership start date.**

**Example:**
- Today: August 1, Start: August 20 → ✅ Allowed.
- Today: August 1, Start: September 15 → ❌ Not allowed because it is more than 30 days in advance.

There is no other arbitrary advance-purchase restriction.

---

## 13. Membership Amount

Every membership must have: `amount >= ₹500`

This applies to:
- standard pricing
- ADMIN overrides
- every membership created

**Examples:**
- ₹500 → ✅
- ₹1,000 → ✅
- ₹499 → ❌

Once membership is created:
- Membership amount is immutable. It cannot later be changed.
- Membership type, start date and end date are also immutable.

---

## 14. Membership Status

Status is derived, not stored.

### Possible statuses:
- UPCOMING
- ACTIVE
- INACTIVE
- EXPIRED

### The rules are evaluated in this order:

1. **Expired**: `endDate < today` → EXPIRED. Expiry takes precedence over the active flag.
2. **Inactive**: If not expired and `active = false` → INACTIVE
3. **Upcoming**: If `startDate > today` and active → UPCOMING
4. **Active**: Otherwise → ACTIVE

---

## 15. Membership Activation / Deactivation

Only ADMIN can perform these operations.

### Activation
Allowed for an inactive membership if it hasn't expired.
- If `startDate > today` → reactivation results in UPCOMING
- If `startDate <= today <= endDate` → reactivation results in ACTIVE
- If expired → EXPIRED, ❌ Cannot reactivate.

### Deactivation
Allowed for: UPCOMING, ACTIVE
- Result: INACTIVE
- Dates remain unchanged. No extension is provided.

---

## 16. Important Upcoming Membership Rule

Suppose:
- Start: September 10
- End: October 9
- active = false
- It is: INACTIVE

When September 10 arrives, it does not automatically become active. It remains: INACTIVE, until ADMIN explicitly activates it. The inactive period is lost. This is intentional for Phase 1.

---

## 17. Invalid Membership State Transitions

These return **400 Bad Request**:
- ACTIVE + activate
- INACTIVE + deactivate
- EXPIRED + activate
- EXPIRED + deactivate

**Examples:**
- ACTIVE → Activate → ❌ 400
- INACTIVE → Deactivate → ❌ 400
- EXPIRED → Activate → ❌ 400

For an upcoming membership: UPCOMING → Activate is allowed and simply remains UPCOMING.

---

## 18. Membership Pricing

Standard pricing is a separate concept.

```
MembershipPricing
-----------------
id
membershipType
price
active
createdAt
updatedAt
```

No `effectiveFrom` / `effectiveTo` — these are intentionally excluded from Phase 1.

---

## 19. Standard Pricing Ownership

Only ADMIN can manage standard pricing. STAFF can consume the configured standard price when creating memberships.

---

## 20. Minimum Standard Price

Every standard price must be `>= ₹500`.

- MONTHLY ₹500 → ✅
- MONTHLY ₹499 → ❌

---

## 21. Standard Price and Membership Amount

The standard price is the default price for a new membership.

**Example:**
- MONTHLY standard price = ₹1,200
- STAFF creates MONTHLY membership → Membership amount = ₹1,200

ADMIN can override it.

**Example:**
- Standard = ₹1,200
- ADMIN chooses = ₹1,000 → Allowed because ₹1,000 >= ₹500

ADMIN can also charge ₹1,500. No override reason is required in Phase 1.

---

## 22. STAFF Pricing Restriction

STAFF cannot override standard pricing.

If standard price is MONTHLY = ₹1,200, STAFF must use ₹1,200. They cannot choose ₹1,000 or ₹1,500.

---

## 23. Missing Standard Price

A membership type may temporarily have no active standard price.

**Example:** MONTHLY → no active price
- STAFF: ❌ Cannot create MONTHLY membership
- ADMIN: ✅ Can create MONTHLY membership, provided ADMIN explicitly supplies `amount >= ₹500`

There is no hard-coded fallback price.

---

## 24. Pricing History

Pricing records are retained historically.

**Example:**
- MONTHLY ₹1,000 → INACTIVE
- MONTHLY ₹1,200 → INACTIVE
- MONTHLY ₹1,500 → ACTIVE

We do not overwrite old records.

---

## 25. One Active Price Per Membership Type

At most one pricing record can be active for a membership type.

**Example:**
- MONTHLY ₹1,200 → ACTIVE
- MONTHLY ₹1,500 → ACTIVE
- ❌ Not allowed.

---

## 26. Pricing Changes

ADMIN can change the standard price at any time.

If MONTHLY ₹1,200 → ACTIVE, and ADMIN creates MONTHLY ₹1,500, the system automatically performs:
- ₹1,200 → INACTIVE
- ₹1,500 → ACTIVE

The old record remains in history. This operation should be transactional.

---

## 27. Duplicate Historical Pricing

This was one of the decisions we explicitly finalized.

Suppose history contains:
- MONTHLY ₹1,000 → INACTIVE
- MONTHLY ₹1,200 → ACTIVE

ADMIN wants ₹1,000 again. We do not create another ₹1,000 record. Instead: reactivate existing ₹1,000 record.

Result:
- MONTHLY ₹1,000 → ACTIVE
- MONTHLY ₹1,200 → INACTIVE

So duplicate `membershipType + price` records are rejected.

---

## 28. Reactivating Historical Pricing

ADMIN can reactivate an inactive historical pricing record.

If another price is currently active:
- Current: ₹1,200 → ACTIVE
- Historical: ₹1,000 → INACTIVE

Reactivate ₹1,000:
- ₹1,200 → INACTIVE
- ₹1,000 → ACTIVE

No duplicate record is created.

---

## 29. Pricing Records Are Immutable

Once created, these cannot be edited: `membershipType`, `price`

**Example:** MONTHLY ₹1,000 cannot be changed directly to MONTHLY ₹1,100.

Instead:
- ₹1,000 → INACTIVE
- ₹1,100 → ACTIVE

or reactivate an existing historical ₹1,100 record. This preserves pricing history.

---

## 30. Pricing Activation/Deactivation

Only ADMIN can do this.

### Active → Activate
❌ 400 Bad Request

**Example:** MONTHLY ₹1,200 → ACTIVE, Activate again → 400

### Inactive → Deactivate
❌ 400 Bad Request

### Inactive → Activate
✅ Allowed.

### Active → Deactivate
✅ Allowed.

---

## 31. No Active Price Is Allowed

ADMIN can deactivate the current standard price without immediately replacing it.

**Example:** MONTHLY ₹1,200 → INACTIVE. Now: No active MONTHLY price. This is valid. ADMIN doesn't have to configure a replacement immediately.

---

## 32. Pricing Deactivation Is Independent of Membership Usage

Even if memberships currently use that price:
- MONTHLY ₹1,200 → ACTIVE
- Current membership → ₹1,200
- Upcoming membership → ₹1,200

ADMIN may still deactivate it: MONTHLY ₹1,200 → INACTIVE

Existing/upcoming memberships remain ₹1,200. Nothing is recalculated.

---

## 33. First Pricing Record

If a membership type has never had pricing (e.g., YEARLY, no pricing history), ADMIN can create YEARLY ₹10,000 → ACTIVE, subject to `price >= ₹500`.

---

## 34. Payment Model

Payment contains:
- id
- member
- membership
- amount
- paymentDate
- paymentMode
- createdAt
- updatedAt

Phase 1 payment mode: **CASH**. Only CASH is supported currently.

---

## 35. Membership + First Payment

Every newly created membership must have a first payment. However, partial payment is allowed.

**Example:**
- Membership amount = ₹1,000
- First payment = ₹500 → ✅ Valid.
- The remaining ₹500 is outstanding.

Membership creation and first payment happen in one database transaction. Therefore: Membership succeeds + Payment succeeds, or: Both rollback.

---

## 36. Membership Amount vs Paid Amount

These are separate concepts.

- **Membership amount**: Total amount committed/payable (e.g., ₹1,000)
- **Payments**: Actual money received (e.g., ₹400, ₹300, ₹300 → Total: ₹1,000)
- **Outstanding**: Membership amount - total payments (e.g., ₹1,000 - ₹700 = ₹300 outstanding)

---

## 37. Payment Rules

### First payment
Must be `>= ₹500` and `<= membership amount`.

**Example:** Membership = ₹1,000
- First payment = ₹500 → ✅
- First payment = ₹700 → ✅
- First payment = ₹1,000 → ✅
- First payment = ₹400 → ❌
- First payment = ₹1,100 → ❌

### Subsequent payments
Can be any positive amount, but cannot exceed outstanding.

**Example:** Membership = ₹1,000, Paid = ₹500, Outstanding = ₹500
- Next payment: ₹300 → ✅
- Next payment: ₹500 → ✅
- Next payment: ₹600 → ❌

---

## 38. Multiple Payments

Multiple payments are allowed.

**Example:** Membership = ₹1,000
- Payment 1 = ₹500
- Payment 2 = ₹200
- Payment 3 = ₹300
- Total: ₹1,000, Status: PAID

---

## 39. Payment Status

Payment status is derived, not stored. Two states: PARTIALLY_PAID, PAID

- If `totalPaid < membership.amount` → PARTIALLY_PAID
- If `totalPaid == membership.amount` → PAID

Payment status is independent of membership status.

---

## 40. Fully Paid Membership

Once `totalPaid = membership.amount`, no additional payment is allowed. Another payment attempt → ❌ 400 Bad Request

---

## 41. Payment Date

Payment date:
- Cannot be in the future.
- Can be today.
- Can be any historical date.
- No artificial historical cutoff exists in Phase 1.

Payment date represents when the money was actually received. It does not determine membership start date.

---

## 42. Payment Before Membership Starts

Allowed.

**Example:**
- Today: August 25
- Payment date: August 25
- Membership start: September 1
- Valid.

Multiple partial payments can also be made before the start date.

---

## 43. Payment on UPCOMING Membership

Allowed.

**Example:**
- Membership: September 1 → September 30, Status: UPCOMING
- Payment: ₹500 → ✅ Allowed.

Payment does not change UPCOMING → ACTIVE. It remains UPCOMING.

---

## 44. Payment on INACTIVE Membership

Allowed.

**Example:**
- Membership → INACTIVE, Outstanding → ₹500
- Payment: ₹500 → ✅ Allowed.

Payment does not reactivate the membership.

---

## 45. Payment for INACTIVE Member

Allowed.

- Member: INACTIVE
- Payment collection: ✅ Allowed

This is intentional because outstanding financial obligations remain even when the member is inactive.

---

## 46. Payment After Membership Expiry

Allowed only if outstanding balance exists. But there is an additional restriction.

If Membership = EXPIRED, Outstanding = ₹500, then payment must be ₹500 in one payment.

- ₹500 → ✅
- ₹300 → ❌

Post-expiry partial payments are not allowed. Payment does not: reactivate membership, extend membership, or change dates. The membership remains EXPIRED.

---

## 47. Outstanding Balance Blocks New Membership

This is a major business rule.

Before creating a new membership, we check all previous memberships. If any membership has `outstanding > ₹0`, new membership creation is rejected. This applies regardless of membership state: ACTIVE, INACTIVE, EXPIRED.

**Example:**
- Old membership: Amount = ₹1,000, Paid = ₹700, Outstanding = ₹300
- Even if it is expired: New membership → ❌ rejected

The ₹300 must first be fully settled.

---

## 48. New Membership After Expiry

Allowed if all previous outstanding balances = ₹0.

**Example:**
- Old membership → EXPIRED, Amount = ₹1,000, Paid = ₹1,000, Outstanding = ₹0
- New membership: ✅ Allowed

---

## 49. Payment and Membership Authorization

| Operation | ADMIN | STAFF |
|---|---|---|
| Membership creation | ✅ | ✅ |
| Membership price override | ✅ | ❌ |
| Payment | ✅ | ✅ |

Both roles can record valid payments, including the final payment.

---

## 50. Payment Immutability

Phase 1 payments cannot be edited or deleted. There is no correction mechanism. Refund/reversal will be addressed in Phase 2.

---

## 51. Membership Immutability

Once created, these cannot be changed: `membershipType`, `startDate`, `endDate`, `amount`

Activation/deactivation only changes the membership's active state. No cancellation or replacement is supported in Phase 1.

---

## 52. Overall Membership Lifecycle

```
                  CREATE
                    │
                    ▼
              ┌───────────┐
              │ UPCOMING  │
              └─────┬─────┘
                    │
             start date arrives
                    │
                    ▼
              ┌───────────┐
              │  ACTIVE   │
              └─────┬─────┘
                    │
             ADMIN deactivate
                    │
                    ▼
              ┌───────────┐
              │ INACTIVE  │
              └─────┬─────┘
                    │
             ADMIN reactivate
                    │
                    ▼
           ACTIVE / UPCOMING

Regardless of active state:

endDate < today
       │
       ▼
   EXPIRED

Expiry always takes precedence.
```

---

## 53. Overall Pricing Lifecycle

```
No pricing
    │
    │ ADMIN creates
    ▼
ACTIVE
    │
    │ ADMIN deactivates
    ▼
INACTIVE
    │
    │ ADMIN reactivates
    ▼
ACTIVE

If ADMIN creates a different price:

₹1,000 ACTIVE
      │
      │ create ₹1,200
      ▼
₹1,000 INACTIVE
₹1,200 ACTIVE

If ₹1,000 already exists historically, it is reactivated, not duplicated.
```

---

## 54. Most Important Architectural Principles

The rules we've agreed on lead to these principles:

### Pricing ≠ Membership amount
```
Standard pricing
      ↓
default for new membership

Membership amount
      ↓
actual agreed price
```
Once membership is created, its amount is locked.

### Member state ≠ Membership state
```
Member → person-level eligibility

Membership → individual membership lifecycle
```

### Membership state ≠ Payment state

A membership can be:
- EXPIRED + PARTIALLY_PAID
- UPCOMING + PAID
- INACTIVE + PARTIALLY_PAID

These are valid combinations.

### Payment ≠ Membership start

A payment can happen before the membership begins.

### Historical data is preserved

We do not overwrite:
- pricing history
- membership amounts
- payment history

---

## Final Implementation Dependency

The implementation order should now be:

```
1. Membership Pricing
        ↓
2. Update Membership creation
   to use pricing rules
        ↓
3. Update Membership lifecycle
   and overlap/outstanding rules
        ↓
4. Integrate first Payment
   into membership creation transaction
        ↓
5. Update Payment rules
        ↓
6. Final validation/error handling
        ↓
7. Business-rule testing
```
