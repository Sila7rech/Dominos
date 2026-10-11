-- =============================================================================
-- DOMINOS • HOUMAFOOD TUNISIA — COMPLETE SUPABASE POSTGRESQL SCHEMA & RLS SCRIPT
-- =============================================================================
-- Instructions:
-- 1. Open your Supabase project dashboard -> SQL Editor -> New Query.
-- 2. Paste this complete script and click "Run".
-- =============================================================================

create extension if not exists "pgcrypto";

-- =============================================================================
-- 1. TABLE DEFINITIONS
-- =============================================================================

-- 1.1 PROFILES
create table if not exists public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    display_name text not null default '',
    phone text not null default '',
    preferred_language text not null default 'fr' check (preferred_language in ('fr', 'ar', 'en')),
    role text not null default 'customer' check (role in ('customer', 'admin')),
    created_at timestamptz not null default now()
);

-- 1.2 CATEGORIES
create table if not exists public.categories (
    id uuid primary key default gen_random_uuid(),
    name_fr text not null,
    name_ar text not null,
    name_en text not null,
    image_url text not null,
    is_active boolean not null default true,
    sort_order integer not null default 0,
    created_at timestamptz not null default now()
);

create index if not exists idx_categories_active_sort
    on public.categories(is_active, sort_order);

-- 1.3 PRODUCTS
create table if not exists public.products (
    id uuid primary key default gen_random_uuid(),
    name_fr text not null,
    name_ar text not null,
    name_en text not null,
    description_fr text not null default '',
    description_ar text not null default '',
    description_en text not null default '',
    price_millimes integer not null check (price_millimes >= 0),
    image_url text not null,
    category_id uuid not null references public.categories(id) on delete cascade,
    is_available boolean not null default true,
    is_featured boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_products_category_available
    on public.products(category_id, is_available);
create index if not exists idx_products_featured
    on public.products(is_featured);

-- 1.4 RESTAURANT SETTINGS
create table if not exists public.restaurant_settings (
    id uuid primary key default gen_random_uuid(),
    name text not null default 'Dominos • HoumaFood',
    logo_url text not null default '',
    phone text not null default '+216 71 800 900',
    address text not null default '12 Avenue Habib Bourguiba, Tunis',
    delivery_fee_millimes integer not null default 4000 check (delivery_fee_millimes >= 0),
    minimum_order_millimes integer not null default 12000 check (minimum_order_millimes >= 0),
    is_open boolean not null default true,
    estimated_delivery_minutes integer not null default 25 check (estimated_delivery_minutes > 0),
    currency text not null default 'TND',
    opening_hours jsonb not null default '{"mon_sun": "11:00 - 00:30"}'::jsonb
);

-- 1.5 ORDERS (Cash on Delivery Only)
create table if not exists public.orders (
    id uuid primary key default gen_random_uuid(),
    order_number text not null unique,
    user_id uuid references auth.users(id) on delete set null,
    customer_name text not null,
    customer_phone text not null,
    delivery_address text not null,
    delivery_instructions text not null default '',
    subtotal_millimes integer not null check (subtotal_millimes >= 0),
    delivery_fee_millimes integer not null check (delivery_fee_millimes >= 0),
    discount_millimes integer not null default 0 check (discount_millimes >= 0),
    total_millimes integer not null check (total_millimes >= 0),
    payment_method text not null default 'cash_on_delivery' check (payment_method = 'cash_on_delivery'),
    payment_status text not null default 'pending_cod' check (payment_status in ('pending_cod', 'paid_cod', 'cancelled')),
    status text not null default 'pending' check (status in ('pending', 'confirmed', 'preparing', 'ready', 'out_for_delivery', 'delivered', 'cancelled')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_orders_user_created
    on public.orders(user_id, created_at desc);
create index if not exists idx_orders_status_created
    on public.orders(status, created_at desc);

-- 1.6 ORDER ITEMS
create table if not exists public.order_items (
    id uuid primary key default gen_random_uuid(),
    order_id uuid not null references public.orders(id) on delete cascade,
    product_id uuid references public.products(id) on delete set null,
    product_name_snapshot jsonb not null,
    image_url_snapshot text not null default '',
    unit_price_millimes integer not null check (unit_price_millimes >= 0),
    quantity integer not null check (quantity > 0),
    selected_options jsonb not null default '{}'::jsonb,
    line_total_millimes integer not null check (line_total_millimes >= 0)
);

create index if not exists idx_order_items_order_id
    on public.order_items(order_id);

-- 1.7 ADDRESSES
create table if not exists public.addresses (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    label text not null default 'Domicile',
    address text not null,
    instructions text not null default '',
    is_default boolean not null default false,
    created_at timestamptz not null default now()
);

create index if not exists idx_addresses_user_id
    on public.addresses(user_id);

-- 1.8 FAVORITES
create table if not exists public.favorites (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    product_id uuid not null references public.products(id) on delete cascade,
    created_at timestamptz not null default now(),
    unique (user_id, product_id)
);

create index if not exists idx_favorites_user_id
    on public.favorites(user_id);

-- =============================================================================
-- 2. HELPER FUNCTIONS & PROFILE AUTO-CREATION TRIGGER
-- =============================================================================

create or replace function public.is_admin()
returns boolean
language sql
security definer
stable
set search_path = public
as $$
    select exists (
        select 1
        from public.profiles
        where id = auth.uid()
          and role = 'admin'
    );
$$;

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.profiles (id, display_name, phone, preferred_language, role)
    values (
        new.id,
        coalesce(new.raw_user_meta_data->>'display_name', split_part(new.email, '@', 1)),
        coalesce(new.raw_user_meta_data->>'phone', ''),
        coalesce(new.raw_user_meta_data->>'preferred_language', 'fr'),
        'customer'
    )
    on conflict (id) do nothing;
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- =============================================================================
-- 3. ROW LEVEL SECURITY (RLS) POLICIES
-- =============================================================================

alter table public.profiles enable row level security;
alter table public.categories enable row level security;
alter table public.products enable row level security;
alter table public.restaurant_settings enable row level security;
alter table public.orders enable row level security;
alter table public.order_items enable row level security;
alter table public.addresses enable row level security;
alter table public.favorites enable row level security;

-- 3.1 PROFILES: Authenticated user can read & update own profile; cannot escalate role
drop policy if exists "Users can read own profile or admin reads all" on public.profiles;
create policy "Users can read own profile or admin reads all"
on public.profiles for select
using (auth.uid() = id or public.is_admin());

drop policy if exists "Users can insert own customer profile" on public.profiles;
create policy "Users can insert own customer profile"
on public.profiles for insert
with check (auth.uid() = id and role = 'customer');

drop policy if exists "Users can update own profile without escalating role" on public.profiles;
create policy "Users can update own profile without escalating role"
on public.profiles for update
using (auth.uid() = id or public.is_admin())
with check (
    public.is_admin()
    or (
        auth.uid() = id
        and role = (select p.role from public.profiles p where p.id = auth.uid())
    )
);

-- 3.2 CATEGORIES: Public read access to active categories; Admin full CRUD
drop policy if exists "Public can read active categories" on public.categories;
create policy "Public can read active categories"
on public.categories for select
using (is_active = true or public.is_admin());

drop policy if exists "Admins manage categories" on public.categories;
create policy "Admins manage categories"
on public.categories for all
using (public.is_admin())
with check (public.is_admin());

-- 3.3 PRODUCTS: Public read access to products in active categories; Admin full CRUD
drop policy if exists "Public can read products" on public.products;
create policy "Public can read products"
on public.products for select
using (
    exists (
        select 1 from public.categories c
        where c.id = products.category_id and c.is_active = true
    )
    or public.is_admin()
);

drop policy if exists "Admins manage products" on public.products;
create policy "Admins manage products"
on public.products for all
using (public.is_admin())
with check (public.is_admin());

-- 3.4 RESTAURANT SETTINGS: Public read access; Admin write access
drop policy if exists "Public can read restaurant settings" on public.restaurant_settings;
create policy "Public can read restaurant settings"
on public.restaurant_settings for select
using (true);

drop policy if exists "Admins update restaurant settings" on public.restaurant_settings;
create policy "Admins update restaurant settings"
on public.restaurant_settings for all
using (public.is_admin())
with check (public.is_admin());

-- 3.5 ORDERS: Authenticated users read/insert their own orders; Admins read/update all
drop policy if exists "Users read own orders or admin reads all" on public.orders;
create policy "Users read own orders or admin reads all"
on public.orders for select
using (auth.uid() = user_id or public.is_admin());

drop policy if exists "Authenticated users can insert own pending COD orders" on public.orders;
create policy "Authenticated users can insert own pending COD orders"
on public.orders for insert
with check (
    auth.uid() = user_id
    and payment_method = 'cash_on_delivery'
    and payment_status = 'pending_cod'
    and status = 'pending'
);

drop policy if exists "Admins update orders" on public.orders;
create policy "Admins update orders"
on public.orders for update
using (public.is_admin())
with check (public.is_admin());

-- 3.6 ORDER ITEMS: Authenticated users read/insert items for their own orders; Admins read all
drop policy if exists "Users read own order items or admin reads all" on public.order_items;
create policy "Users read own order items or admin reads all"
on public.order_items for select
using (
    exists (
        select 1 from public.orders o
        where o.id = order_items.order_id
          and (o.user_id = auth.uid() or public.is_admin())
    )
);

drop policy if exists "Users insert items for own pending orders" on public.order_items;
create policy "Users insert items for own pending orders"
on public.order_items for insert
with check (
    exists (
        select 1 from public.orders o
        where o.id = order_items.order_id
          and o.user_id = auth.uid()
          and o.status = 'pending'
    )
);

-- 3.7 ADDRESSES: Authenticated users manage only their own addresses
drop policy if exists "Users manage own addresses" on public.addresses;
create policy "Users manage own addresses"
on public.addresses for all
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

-- 3.8 FAVORITES: Authenticated users manage only their own favorites
drop policy if exists "Users manage own favorites" on public.favorites;
create policy "Users manage own favorites"
on public.favorites for all
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

-- =============================================================================
-- 4. TRANSACTION-SAFE ORDER CREATION RPC FUNCTION
-- =============================================================================
create or replace function public.create_verified_order(
    p_customer_name text,
    p_customer_phone text,
    p_delivery_address text,
    p_delivery_instructions text,
    p_promo_code text,
    p_items jsonb
)
returns public.orders
language plpgsql
security definer
set search_path = public
as $$
declare
    v_settings public.restaurant_settings;
    v_subtotal integer := 0;
    v_discount integer := 0;
    v_delivery_fee integer := 0;
    v_total integer := 0;
    v_item jsonb;
    v_product public.products;
    v_qty integer;
    v_line_total integer;
    v_order public.orders;
    v_order_number text;
begin
    select * into v_settings from public.restaurant_settings limit 1;
    if not found or not v_settings.is_open then
        raise exception 'RESTAURANT_CLOSED';
    end if;

    for v_item in select * from jsonb_array_elements(p_items)
    loop
        v_qty := (v_item->>'quantity')::integer;
        if v_qty <= 0 then
            raise exception 'INVALID_QUANTITY';
        end if;

        select * into v_product
        from public.products
        where id = (v_item->>'product_id')::uuid and is_available = true;

        if not found then
            raise exception 'PRODUCT_UNAVAILABLE';
        end if;

        v_line_total := v_product.price_millimes * v_qty;
        v_subtotal := v_subtotal + v_line_total;
    end loop;

    if v_subtotal < v_settings.minimum_order_millimes then
        raise exception 'MIN_ORDER_NOT_MET';
    end if;

    v_delivery_fee := v_settings.delivery_fee_millimes;
    if upper(coalesce(p_promo_code, '')) = 'FOODIE30' then
        v_discount := (v_subtotal * 30) / 100;
    elsif upper(coalesce(p_promo_code, '')) = 'FREESHIP' then
        v_delivery_fee := 0;
    end if;

    v_total := greatest(0, v_subtotal - v_discount + v_delivery_fee);
    v_order_number := 'DOM-TN-' || floor(random() * 9000 + 1000)::text;

    insert into public.orders (
        order_number,
        user_id,
        customer_name,
        customer_phone,
        delivery_address,
        delivery_instructions,
        subtotal_millimes,
        delivery_fee_millimes,
        discount_millimes,
        total_millimes,
        payment_method,
        payment_status,
        status
    ) values (
        v_order_number,
        auth.uid(),
        p_customer_name,
        p_customer_phone,
        p_delivery_address,
        coalesce(p_delivery_instructions, ''),
        v_subtotal,
        v_delivery_fee,
        v_discount,
        v_total,
        'cash_on_delivery',
        'pending_cod',
        'pending'
    ) returning * into v_order;

    for v_item in select * from jsonb_array_elements(p_items)
    loop
        v_qty := (v_item->>'quantity')::integer;
        select * into v_product from public.products where id = (v_item->>'product_id')::uuid;

        insert into public.order_items (
            order_id,
            product_id,
            product_name_snapshot,
            image_url_snapshot,
            unit_price_millimes,
            quantity,
            selected_options,
            line_total_millimes
        ) values (
            v_order.id,
            v_product.id,
            jsonb_build_object('fr', v_product.name_fr, 'ar', v_product.name_ar, 'en', v_product.name_en),
            v_product.image_url,
            v_product.price_millimes,
            v_qty,
            jsonb_build_object('summary', coalesce(v_item->>'selected_options', '')),
            v_product.price_millimes * v_qty
        );
    end loop;

    return v_order;
end;
$$;

-- =============================================================================
-- 5. INITIAL SEED DATA (RESTAURANT SETTINGS, CATEGORIES & PRODUCTS)
-- =============================================================================
insert into public.restaurant_settings (
    id, name, logo_url, phone, address, delivery_fee_millimes, minimum_order_millimes, is_open, estimated_delivery_minutes, currency
) values (
    '00000000-0000-0000-0000-000000000010',
    'Dominos • HoumaFood',
    'https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=400&q=80',
    '+216 71 800 900',
    '12 Avenue Habib Bourguiba, Tunis 1000, Tunisie',
    4000,
    12000,
    true,
    25,
    'TND'
) on conflict (id) do nothing;

insert into public.categories (id, name_fr, name_ar, name_en, image_url, is_active, sort_order) values
    ('00000000-0000-0000-0000-000000000101', 'Pizza', 'بيتزا', 'Pizza', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=500&q=80', true, 1),
    ('00000000-0000-0000-0000-000000000102', 'Burger', 'برغر', 'Burger', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=500&q=80', true, 2),
    ('00000000-0000-0000-0000-000000000103', 'Sushi', 'سوشي', 'Sushi', 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=500&q=80', true, 3),
    ('00000000-0000-0000-0000-000000000104', 'Poulet', 'دجاج', 'Chicken', 'https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?auto=format&fit=crop&w=500&q=80', true, 4)
on conflict (id) do nothing;

insert into public.products (
    id, name_fr, name_ar, name_en, description_fr, description_ar, description_en, price_millimes, image_url, category_id, is_available, is_featured
) values
    (
        '00000000-0000-0000-0000-000000000201',
        'Pepperoni Feast',
        'بيبروني فيست',
        'Pepperoni Feast',
        'Pizza signature Dominos garnie de double pepperoni de bœuf halal et mozzarella fondante.',
        'بيتزا دومينوز الشهيرة مع شرائح البيبروني الحلال المزدوجة وجبنة الموزاريلا الذائبة.',
        'Signature Dominos pizza topped with double halal beef pepperoni and melted mozzarella.',
        19900,
        'https://images.unsplash.com/photo-1628840042765-356cda07504e?auto=format&fit=crop&w=700&q=80',
        '00000000-0000-0000-0000-000000000101',
        true,
        true
    ),
    (
        '00000000-0000-0000-0000-000000000202',
        'Cheesy Garlic Bread',
        'خبز بالثوم والجبن',
        'Cheesy Garlic Bread',
        'Baguette artisanale dorée au four avec beurre à l''ail rôti et mozzarella gratinée.',
        'خبز باغيت محمص في الفرن بالزبدة والثوم مع طبقة غنية من جبنة الموزاريلا.',
        'Oven-baked artisan baguette brushed with roasted garlic butter and gratinated mozzarella.',
        7900,
        'https://images.unsplash.com/photo-1573140247632-f8fd74997d5c?auto=format&fit=crop&w=700&q=80',
        '00000000-0000-0000-0000-000000000101',
        true,
        true
    ),
    (
        '00000000-0000-0000-0000-000000000203',
        'Sushi Bento Box',
        'سوشي بينتو بوكس',
        'Sushi Bento Box',
        'Coffret complet avec nigiri au saumon frais, california rolls et salade wakamé.',
        'علبة سوشي متكاملة تحتوي على نيجيري السلمون الطازج وكاليفورنيا رول وسلطة واكامي.',
        'Complete bento box featuring fresh salmon nigiri, california rolls, and wakame salad.',
        26900,
        'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=700&q=80',
        '00000000-0000-0000-0000-000000000103',
        true,
        true
    ),
    (
        '00000000-0000-0000-0000-000000000204',
        'Cheese Burger Gourmet',
        'تشيز برغر غورميه',
        'Cheese Burger',
        'Steak haché pur bœuf grillé à la flamme, double cheddar fondant sur pain brioché.',
        'شريحة لحم بقري مشوية على اللهب مع جبن شيدر مزدوج في خبز بريوش محمص.',
        'Flame-grilled pure beef patty and double melted cheddar on a toasted brioche bun.',
        14900,
        'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=700&q=80',
        '00000000-0000-0000-0000-000000000102',
        true,
        true
    )
on conflict (id) do nothing;
