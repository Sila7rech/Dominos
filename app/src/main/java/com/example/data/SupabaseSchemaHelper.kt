package com.example.data

object SupabaseSchemaHelper {

    /**
     * Complete, self-contained SQL script for Supabase SQL Editor.
     * Creates all 8 tables (`profiles`, `categories`, `products`, `restaurant_settings`,
     * `orders`, `order_items`, `addresses`, `favorites`), auth trigger for new accounts,
     * RLS policies, and initial seed data so the database is populated immediately.
     */
    const val FULL_SUPABASE_SQL_SCRIPT = """
-- ============================================================================
-- DOMINOS TUNISIE - SCRIPT SQL COMPLET SUPABASE (TABLES + RLS + DONNEES)
-- Copiez tout ce script dans Supabase Dashboard -> SQL Editor -> New Query -> Run
-- ============================================================================

create extension if not exists "pgcrypto";

-- 1. TABLE PROFILES (Comptes utilisateurs liés à auth.users)
create table if not exists public.profiles (
    id uuid primary key,
    display_name text not null default '',
    phone text not null default '',
    preferred_language text not null default 'fr' check (preferred_language in ('fr', 'ar', 'en')),
    role text not null default 'customer' check (role in ('customer', 'admin')),
    created_at timestamptz not null default now()
);

-- 2. TABLE CATEGORIES
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

-- 3. TABLE PRODUCTS
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

-- 4. TABLE RESTAURANT_SETTINGS
create table if not exists public.restaurant_settings (
    id uuid primary key default gen_random_uuid(),
    name text not null default 'Dominos Tunisie',
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

-- 5. TABLE ORDERS (Paiement à la livraison COD)
create table if not exists public.orders (
    id uuid primary key default gen_random_uuid(),
    order_number text not null unique,
    user_id uuid,
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

-- 6. TABLE ORDER_ITEMS
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

-- 7. TABLE ADDRESSES (Adresses GPS & Livraison)
create table if not exists public.addresses (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null,
    label text not null default 'GPS',
    address text not null,
    instructions text not null default '',
    is_default boolean not null default false,
    created_at timestamptz not null default now()
);

-- 8. TABLE FAVORITES
create table if not exists public.favorites (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null,
    product_id uuid not null references public.products(id) on delete cascade,
    created_at timestamptz not null default now(),
    unique (user_id, product_id)
);

-- TRIGGER AUTOMATIQUE DE CREATION DE PROFIL LORS D'UN SIGNUP AUTH
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
    insert into public.profiles (id, display_name, phone, preferred_language, role)
    values (
        new.id,
        coalesce(new.raw_user_meta_data->>'display_name', split_part(new.email, '@', 1)),
        coalesce(new.raw_user_meta_data->>'phone', '+216 22 456 789'),
        coalesce(new.raw_user_meta_data->>'preferred_language', 'fr'),
        case when new.email ilike '%admin%' then 'admin' else 'customer' end
    ) on conflict (id) do update set
        display_name = excluded.display_name,
        phone = excluded.phone;
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- POLITIQUES RLS (ROW LEVEL SECURITY)
alter table public.profiles enable row level security;
alter table public.categories enable row level security;
alter table public.products enable row level security;
alter table public.restaurant_settings enable row level security;
alter table public.orders enable row level security;
alter table public.order_items enable row level security;
alter table public.addresses enable row level security;
alter table public.favorites enable row level security;

drop policy if exists "Public categories access" on public.categories;
create policy "Public categories access" on public.categories for all using (true) with check (true);

drop policy if exists "Public products access" on public.products;
create policy "Public products access" on public.products for all using (true) with check (true);

drop policy if exists "Public settings access" on public.restaurant_settings;
create policy "Public settings access" on public.restaurant_settings for all using (true) with check (true);

drop policy if exists "Profiles access" on public.profiles;
create policy "Profiles access" on public.profiles for all using (true) with check (true);

drop policy if exists "Orders access" on public.orders;
create policy "Orders access" on public.orders for all using (true) with check (true);

drop policy if exists "Order items access" on public.order_items;
create policy "Order items access" on public.order_items for all using (true) with check (true);

drop policy if exists "Addresses access" on public.addresses;
create policy "Addresses access" on public.addresses for all using (true) with check (true);

drop policy if exists "Favorites access" on public.favorites;
create policy "Favorites access" on public.favorites for all using (true) with check (true);

-- DONNEES INITIALES (CATEGORIES, PARAMETRES RESTAURANT & PRODUITS DOMINOS)
insert into public.restaurant_settings (id, name, logo_url, phone, address, delivery_fee_millimes, minimum_order_millimes, is_open, estimated_delivery_minutes, currency)
values ('11111111-1111-1111-1111-111111111111', 'Dominos Tunisie', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=600&q=80', '+216 71 800 900', '12 Avenue Habib Bourguiba, Tunis', 4000, 12000, true, 25, 'TND')
on conflict (id) do nothing;

insert into public.categories (id, name_fr, name_ar, name_en, image_url, is_active, sort_order) values
('a1111111-1111-1111-1111-111111111111', 'Pizza', 'بيتزا', 'Pizza', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=600&q=80', true, 1),
('a2222222-2222-2222-2222-222222222222', 'Burger', 'برغر', 'Burger', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80', true, 2),
('a3333333-3333-3333-3333-333333333333', 'Poulet & Sides', 'دجاج ومقبلات', 'Chicken & Sides', 'https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=600&q=80', true, 3),
('a4444444-4444-4444-4444-444444444444', 'Tunisien', 'أطباق تونسية', 'Tunisian', 'https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=600&q=80', true, 4)
on conflict (id) do nothing;

insert into public.products (id, name_fr, name_ar, name_en, description_fr, description_ar, description_en, price_millimes, image_url, category_id, is_available, is_featured) values
('b1111111-1111-1111-1111-111111111111', 'Pizza Pepperoni Feast', 'بيتزا بيبروني فيست', 'Pepperoni Feast Pizza', 'Double pepperoni croustillant, mozzarella fondante et sauce tomate Domino''s aux herbes.', 'دبل بيبروني مقرمش مع جبنة موزاريلا ذائبة وصلصة طماطم دومينوز.', 'Double crispy pepperoni, melted mozzarella and signature Domino''s herb tomato sauce.', 21900, 'https://images.unsplash.com/photo-1628840042765-356cda07504e?auto=format&fit=crop&w=800&q=80', 'a1111111-1111-1111-1111-111111111111', true, true),
('b2222222-2222-2222-2222-222222222222', 'Pizza Tunisienne Harissa & Thon', 'بيتزا تونسية بالتن والهريسة', 'Tunisian Tuna & Harissa Pizza', 'Thon entier, olives noires de Tunisie, poivrons frais, oignons rouges, mozzarella et touche d''harissa.', 'تن فاخر، زيتون أسود تونسي، فلفل أخضر، بصل، موزاريلا ولمسة هريسة.', 'Premium tuna, Tunisian black olives, fresh bell peppers, red onions, mozzarella and harissa.', 19500, 'https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=800&q=80', 'a1111111-1111-1111-1111-111111111111', true, true),
('b3333333-3333-3333-3333-333333333333', 'Pizza 4 Fromages Royale', 'بيتزا 4 أجبان رويال', 'Royal 4 Cheese Pizza', 'Mélange généreux de mozzarella, cheddar affiné, emmental et bleu sur pâte fraîche.', 'مزيج غني من الموزاريلا والشيدر والإيمونتال والجبن الأزرق.', 'Generous blend of mozzarella, aged cheddar, emmental and blue cheese on fresh dough.', 23500, 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?auto=format&fit=crop&w=800&q=80', 'a1111111-1111-1111-1111-111111111111', true, false),
('b4444444-4444-4444-4444-444444444444', 'Double Cheese Burger', 'دبل تشيز برغر', 'Double Cheese Burger', 'Steak grillé à la flamme, double cheddar fondant, cornichons, salade croquante et sauce fumée.', 'لحم بقري مشوي مع دبل جبنة شيدر ومخلل وصلصة مدخنة.', 'Flame-grilled beef patty, double melted cheddar, pickles, crisp lettuce and smoky sauce.', 14900, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=800&q=80', 'a2222222-2222-2222-2222-222222222222', true, true),
('b5555555-5555-5555-5555-555555555555', 'Crispy Chicken Wings (8 pcs)', 'أجنحة دجاج مقرمشة (8 قطع)', 'Crispy Chicken Wings (8 pcs)', 'Ailes de poulet marinées aux épices Domino''s, servies chaudes avec sauce BBQ.', 'أجنحة دجاج متبلة بتوابل دومينوز تقدم ساخنة مع صلصة باربكيو.', 'Marinated Domino''s spiced chicken wings served hot with BBQ dip.', 13900, 'https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=800&q=80', 'a3333333-3333-3333-3333-333333333333', true, true),
('b6666666-6666-6666-6666-666666666666', 'Makloub Escalope Grillée', 'مقلوب إسكالوب مشوي', 'Grilled Chicken Makloub', 'Pain pâte à pizza Domino''s cuit au four, escalope grillée, mozzarella, frites et méchouia.', 'خبز عجينة بيتزا دومينوز في الفرن مع إسكالوب مشوي وموزاريلا وسلطة مشوية.', 'Oven-baked Domino''s pizza dough flatbread stuffed with grilled chicken, mozzarella and mechouia.', 12500, 'https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=800&q=80', 'a4444444-4444-4444-4444-444444444444', true, true)
on conflict (id) do nothing;
"""
}
