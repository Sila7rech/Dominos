package com.example.data

import com.example.model.AppLanguage
import com.example.model.Category
import com.example.model.OrderItemRecord
import com.example.model.OrderRecord
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.ProductExtra
import com.example.model.ProductVariant
import com.example.model.PromoDeal
import com.example.model.RestaurantSettings
import com.example.model.SavedAddress
import com.example.model.UserProfile
import com.example.model.UserRole

object SampleTunisiaData {

    val defaultRestaurantSettings = RestaurantSettings(
        id = "rest-dominos-tunis-01",
        name = "Dominos • HoumaFood",
        logoUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=400&q=80",
        phone = "+216 71 800 900",
        address = "12 Avenue Habib Bourguiba, Tunis 1000, Tunisie",
        deliveryFeeMillimes = 4000, // 4,000 DT
        minimumOrderMillimes = 12000, // 12,000 DT
        isOpen = true,
        estimatedDeliveryMinutes = 25,
        currency = "TND",
        openingHoursJson = "Lun - Dim : 11h00 - 00h30",
        whatsappUrl = "https://wa.me/21671800900"
    )

    val demoCustomerProfile = UserProfile(
        id = "00000000-0000-0000-0000-000000000101",
        email = "client@houmafood.tn",
        displayName = "Yassine Ben Ali",
        phone = "+216 22 456 789",
        preferredLanguage = AppLanguage.FR,
        role = UserRole.CUSTOMER
    )

    val demoAdminProfile = UserProfile(
        id = "00000000-0000-0000-0000-000000000001",
        email = "admin@dominos.tn",
        displayName = "Admin Dominos Tunisie",
        phone = "+216 71 800 900",
        preferredLanguage = AppLanguage.FR,
        role = UserRole.ADMIN
    )

    val initialAddresses = listOf(
        SavedAddress(
            id = "addr-home-1",
            userId = demoCustomerProfile.id,
            label = "Domicile",
            address = "12 Avenue Habib Bourguiba, El Menzah 6, Tunis",
            instructions = "Sonner à l'interphone 2ème étage",
            isDefault = true
        ),
        SavedAddress(
            id = "addr-work-2",
            userId = demoCustomerProfile.id,
            label = "Bureau",
            address = "Les Berges du Lac 2, Rue du Lac Léman, Tunis",
            instructions = "Appeler à l'accueil",
            isDefault = false
        )
    )

    val initialCategories = listOf(
        Category(
            id = "cat-pizza",
            nameFr = "Pizza",
            nameAr = "بيتزا",
            nameEn = "Pizza",
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=500&q=80",
            emojiIcon = "🍕",
            isActive = true,
            sortOrder = 1
        ),
        Category(
            id = "cat-burger",
            nameFr = "Burger",
            nameAr = "برغر",
            nameEn = "Burger",
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=500&q=80",
            emojiIcon = "🍔",
            isActive = true,
            sortOrder = 2
        ),
        Category(
            id = "cat-sushi",
            nameFr = "Sushi",
            nameAr = "سوشي",
            nameEn = "Sushi",
            imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=500&q=80",
            emojiIcon = "🍣",
            isActive = true,
            sortOrder = 3
        ),
        Category(
            id = "cat-chicken",
            nameFr = "Poulet",
            nameAr = "دجاج",
            nameEn = "Chicken",
            imageUrl = "https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?auto=format&fit=crop&w=500&q=80",
            emojiIcon = "🍗",
            isActive = true,
            sortOrder = 4
        ),
        Category(
            id = "cat-tunisian",
            nameFr = "Tunisien",
            nameAr = "تونسي",
            nameEn = "Tunisian",
            imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=500&q=80",
            emojiIcon = "🌶️",
            isActive = true,
            sortOrder = 5
        )
    )

    private val pizzaSizeVariants = listOf(
        ProductVariant(
            id = "var-medium",
            nameFr = "Moyenne (30 cm)",
            nameAr = "متوسطة (30 سم)",
            nameEn = "Medium (30 cm)",
            priceDeltaMillimes = 0
        ),
        ProductVariant(
            id = "var-large",
            nameFr = "Grande (36 cm)",
            nameAr = "كبيرة (36 سم)",
            nameEn = "Large (36 cm)",
            priceDeltaMillimes = 5500
        ),
        ProductVariant(
            id = "var-cheesy-crust",
            nameFr = "Méga Croûte Fromagère",
            nameAr = "عجينة محشوة بالجبن",
            nameEn = "Cheesy Crust Mega",
            priceDeltaMillimes = 8000
        )
    )

    private val pizzaExtras = listOf(
        ProductExtra(
            id = "ext-mozzarella",
            nameFr = "Double Mozzarella",
            nameAr = "جبنة موزاريلا إضافية",
            nameEn = "Extra Mozzarella",
            priceMillimes = 3000
        ),
        ProductExtra(
            id = "ext-harissa",
            nameFr = "Sauce Harissa Maison",
            nameAr = "هريسة تونسية",
            nameEn = "House Harissa Sauce",
            priceMillimes = 1000
        ),
        ProductExtra(
            id = "ext-olives",
            nameFr = "Olives de Tunisie",
            nameAr = "زيتون تونسي",
            nameEn = "Tunisian Olives",
            priceMillimes = 1500
        )
    )

    private val burgerExtras = listOf(
        ProductExtra(
            id = "ext-cheddar",
            nameFr = "Cheddar Affiné",
            nameAr = "جبن شيدر إضافي",
            nameEn = "Extra Aged Cheddar",
            priceMillimes = 2000
        ),
        ProductExtra(
            id = "ext-fries",
            nameFr = "Portion Frites Croustillantes",
            nameAr = "بطاطا مقلية مقرمشة",
            nameEn = "Crispy French Fries",
            priceMillimes = 3500
        )
    )

    val initialProducts = listOf(
        Product(
            id = "prod-pepperoni-feast",
            nameFr = "Pepperoni Feast",
            nameAr = "بيبروني فيست",
            nameEn = "Pepperoni Feast",
            descriptionFr = "Pizza signature Dominos garnie de double pepperoni de bœuf halal, mozzarella fondante et sauce tomate aux herbes méditerranéennes.",
            descriptionAr = "بيتزا دومينوز الشهيرة مع شرائح البيبروني الحلال المزدوجة وجبنة الموزاريلا الذائبة وصلصة الطماطم بالأعشاب.",
            descriptionEn = "Signature Dominos pizza topped with double halal beef pepperoni, melted mozzarella, and Mediterranean herb tomato sauce.",
            priceMillimes = 19900, // 19,900 DT
            imageUrl = "https://images.unsplash.com/photo-1628840042765-356cda07504e?auto=format&fit=crop&w=700&q=80",
            categoryId = "cat-pizza",
            isAvailable = true,
            isFeatured = true,
            calories = 310,
            prepMinutes = 20,
            variants = pizzaSizeVariants,
            extras = pizzaExtras
        ),
        Product(
            id = "prod-cheesy-garlic-bread",
            nameFr = "Cheesy Garlic Bread",
            nameAr = "خبز بالثوم والجبن",
            nameEn = "Cheesy Garlic Bread",
            descriptionFr = "Baguette artisanale dorée au four avec beurre à l'ail rôti, persil frais et généreuse couche de mozzarella gratinée.",
            descriptionAr = "خبز باغيت محمص في الفرن بالزبدة والثوم والبقدونس الطازج مع طبقة غنية من جبنة الموزاريلا.",
            descriptionEn = "Oven-baked artisan baguette brushed with roasted garlic butter, fresh parsley, and gratinated mozzarella.",
            priceMillimes = 7900, // 7,900 DT
            imageUrl = "https://images.unsplash.com/photo-1573140247632-f8fd74997d5c?auto=format&fit=crop&w=700&q=80",
            categoryId = "cat-pizza",
            isAvailable = true,
            isFeatured = true,
            calories = 280,
            prepMinutes = 12,
            variants = emptyList(), // No invented options for simple sides
            extras = emptyList()
        ),
        Product(
            id = "prod-sushi-bento-box",
            nameFr = "Sushi Bento Box",
            nameAr = "سوشي بينتو بوكس",
            nameEn = "Sushi Bento Box",
            descriptionFr = "Coffret complet avec nigiri au saumon frais, california rolls, riz vinaigré japonais, salade wakamé et sauce soja.",
            descriptionAr = "علبة سوشي متكاملة تحتوي على نيجيري السلمون الطازج وكاليفورنيا رول وأرز ياباني وسلطة واكامي.",
            descriptionEn = "Complete bento box featuring fresh salmon nigiri, california rolls, seasoned sushi rice, wakame salad, and soy dipping sauce.",
            priceMillimes = 26900, // 26,900 DT
            imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=700&q=80",
            categoryId = "cat-sushi",
            isAvailable = true,
            isFeatured = true,
            calories = 610,
            prepMinutes = 25,
            variants = emptyList(),
            extras = emptyList()
        ),
        Product(
            id = "prod-salmon-avocado-maki",
            nameFr = "Salmon Avocado Maki",
            nameAr = "ماكي السلمون والأفوكادو",
            nameEn = "Salmon Avocado Maki",
            descriptionFr = "8 rouleaux de maki préparés à la minute avec saumon de Norvège, avocat crémeux et graines de sésame grillées.",
            descriptionAr = "8 قطع ماكي محضرة طازجة بالسلمون النرويجي والأفوكادو الكريمي وبذور السمسم المحمصة.",
            descriptionEn = "8 freshly rolled maki pieces with Norwegian salmon, creamy avocado, and toasted sesame seeds.",
            priceMillimes = 22900, // 22,900 DT
            imageUrl = "https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?auto=format&fit=crop&w=700&q=80",
            categoryId = "cat-sushi",
            isAvailable = true,
            isFeatured = false,
            calories = 420,
            prepMinutes = 20,
            variants = emptyList(),
            extras = emptyList()
        ),
        Product(
            id = "prod-prawn-tempura",
            nameFr = "Prawn Tempura",
            nameAr = "جمبري تمبورا مقرمش",
            nameEn = "Prawn Tempura",
            descriptionFr = "Crevettes royales de Méditerranée enrobées d'une pâte tempura légère et croustillante, servies avec sauce chili douce.",
            descriptionAr = "جمبري البحر الأبيض المتوسط مقلي بعجينة التمبورا الخفيفة والمقرمشة يقدم مع صلصة الفلفل الحلو.",
            descriptionEn = "Mediterranean king prawns coated in light, crispy tempura batter, served with sweet chili dipping sauce.",
            priceMillimes = 21500, // 21,500 DT
            imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?auto=format&fit=crop&w=700&q=80",
            categoryId = "cat-sushi",
            isAvailable = true,
            isFeatured = false,
            calories = 380,
            prepMinutes = 18,
            variants = emptyList(),
            extras = emptyList()
        ),
        Product(
            id = "prod-crispy-fried-chicken",
            nameFr = "Crispy Fried Chicken",
            nameAr = "دجاج مقرمش ذهبي",
            nameEn = "Crispy Fried Chicken",
            descriptionFr = "Pilons et filets de poulet marinés aux épices douces, panure extra-croustillante dorée à souhait.",
            descriptionAr = "قطع دجاج متبلة بالبهارات الخاصة ومقلية بطبقة مقرمشة ذهبية شهية.",
            descriptionEn = "Drumsticks and chicken tenders marinated in savory spices with an extra-crispy golden crust.",
            priceMillimes = 15900, // 15,900 DT
            imageUrl = "https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?auto=format&fit=crop&w=700&q=80",
            categoryId = "cat-chicken",
            isAvailable = true,
            isFeatured = true,
            calories = 480,
            prepMinutes = 18,
            variants = emptyList(),
            extras = burgerExtras
        ),
        Product(
            id = "prod-cheese-burger",
            nameFr = "Cheese Burger Gourmet",
            nameAr = "تشيز برغر غورميه",
            nameEn = "Cheese Burger",
            descriptionFr = "Steak haché pur bœuf grillé à la flamme, double cheddar fondant, cornichons croquants, sauce secrète sur pain brioché toasté.",
            descriptionAr = "شريحة لحم بقري مشوية على اللهب مع جبن شيدر مزدوج ومخلل وصلصة خاصة في خبز بريوش محمص.",
            descriptionEn = "Flame-grilled pure beef patty, double melted cheddar, crunchy pickles, and secret sauce on a toasted brioche bun.",
            priceMillimes = 14900, // 14,900 DT
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=700&q=80",
            categoryId = "cat-burger",
            isAvailable = true,
            isFeatured = true,
            calories = 540,
            prepMinutes = 15,
            variants = emptyList(),
            extras = burgerExtras
        ),
        Product(
            id = "prod-pizza-neptuno-tn",
            nameFr = "Pizza Neptuno Tunisienne",
            nameAr = "بيتزا نبتونو تونسية بالتن",
            nameEn = "Tunisian Neptuno Pizza",
            descriptionFr = "La préférée en Tunisie : thon entier de qualité, olives noires, poivrons verts frais, oignons doux, câpres et mozzarella.",
            descriptionAr = "المفضلة في تونس: تن فاخر، زيتون أسود، فلفل أخضر طازج، بصل، كبار وجبنة موزاريلا.",
            descriptionEn = "Tunisia's favorite pizza topped with premium chunk tuna, black olives, green bell peppers, sweet onions, capers, and mozzarella.",
            priceMillimes = 17500, // 17,500 DT
            imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=700&q=80",
            categoryId = "cat-tunisian",
            isAvailable = true,
            isFeatured = true,
            calories = 340,
            prepMinutes = 18,
            variants = pizzaSizeVariants,
            extras = pizzaExtras
        ),
        Product(
            id = "prod-makloub-escalope",
            nameFr = "Makloub Escalope Grillée",
            nameAr = "مقلوب إسكالوب مشوي",
            nameEn = "Grilled Chicken Makloub",
            descriptionFr = "Pâte à pizza fraîche repliée au four au feu de bois, garnie d'escalope grillée, mozzarella, salade méchouia et harissa.",
            descriptionAr = "عجينة بيتزا طازجة مطوية في الفرن محشوة بإسكالوب الدجاج المشوي والموزاريلا وسلطة مشوية وهريسة.",
            descriptionEn = "Wood-fired folded pizza dough stuffed with grilled chicken escalope, mozzarella, mechouia salad, and harissa.",
            priceMillimes = 12500, // 12,500 DT
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=700&q=80",
            categoryId = "cat-tunisian",
            isAvailable = true,
            isFeatured = false,
            calories = 520,
            prepMinutes = 15,
            variants = emptyList(),
            extras = pizzaExtras
        )
    )

    val promoDeals = listOf(
        PromoDeal(
            code = "FOODIE30",
            badgeFr = "SUR TOUTE LA COMMANDE",
            badgeAr = "على كامل طلبيتك",
            badgeEn = "YOUR WHOLE ORDER",
            headlineFr = "30% OFF",
            headlineAr = "تخفيض 30%",
            headlineEn = "30% OFF",
            discountPercent = 30,
            isFreeDelivery = false,
            imageUrl = "https://images.unsplash.com/photo-1628840042765-356cda07504e?auto=format&fit=crop&w=600&q=80"
        ),
        PromoDeal(
            code = "FREESHIP",
            badgeFr = "FRAIS DE LIVRAISON",
            badgeAr = "معلوم التوصيل",
            badgeEn = "DELIVERY FEE",
            headlineFr = "FREE",
            headlineAr = "مجاني",
            headlineEn = "FREE",
            discountPercent = 0,
            isFreeDelivery = true,
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=600&q=80"
        )
    )

    val initialDemoOrders = listOf(
        OrderRecord(
            id = "ord-demo-1001",
            orderNumber = "DOM-TN-8421",
            userId = demoCustomerProfile.id,
            customerName = "Yassine Ben Ali",
            customerPhone = "+216 22 456 789",
            deliveryAddress = "12 Avenue Habib Bourguiba, El Menzah 6, Tunis",
            deliveryInstructions = "Sonner à l'interphone 2ème étage",
            subtotalMillimes = 27800,
            deliveryFeeMillimes = 4000,
            discountMillimes = 0,
            totalMillimes = 31800,
            paymentMethod = "cash_on_delivery",
            paymentStatus = "pending_cod",
            status = OrderStatus.PREPARING,
            createdAt = "2026-10-11 12:15",
            updatedAt = "2026-10-11 12:22",
            isLocalDemo = true,
            items = listOf(
                OrderItemRecord(
                    id = "item-1",
                    orderId = "ord-demo-1001",
                    productId = "prod-pepperoni-feast",
                    productNameFr = "Pepperoni Feast",
                    productNameAr = "بيبروني فيست",
                    productNameEn = "Pepperoni Feast",
                    imageUrlSnapshot = "https://images.unsplash.com/photo-1628840042765-356cda07504e?auto=format&fit=crop&w=700&q=80",
                    unitPriceMillimes = 19900,
                    quantity = 1,
                    selectedOptionsSummary = "Moyenne (30 cm)",
                    lineTotalMillimes = 19900
                ),
                OrderItemRecord(
                    id = "item-2",
                    orderId = "ord-demo-1001",
                    productId = "prod-cheesy-garlic-bread",
                    productNameFr = "Cheesy Garlic Bread",
                    productNameAr = "خبز بالثوم والجبن",
                    productNameEn = "Cheesy Garlic Bread",
                    imageUrlSnapshot = "https://images.unsplash.com/photo-1573140247632-f8fd74997d5c?auto=format&fit=crop&w=700&q=80",
                    unitPriceMillimes = 7900,
                    quantity = 1,
                    selectedOptionsSummary = "",
                    lineTotalMillimes = 7900
                )
            )
        )
    )
}
