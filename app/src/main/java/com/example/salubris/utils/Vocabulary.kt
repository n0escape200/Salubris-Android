package com.example.salubris.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Vocabulary {
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    fun get(): Strings = when (_currentLanguage.value) {
        "en" -> englishStrings
        "es" -> spanishStrings
        "fr" -> frenchStrings
        "de" -> germanStrings
        "ro" -> romanianStrings
        else -> englishStrings
    }

    // ---------- Strings container ----------
    class Strings {
        // General
        var appName: String by notNull()
        var close: String by notNull()
        var cancel: String by notNull()
        var confirm: String by notNull()
        var save: String by notNull()
        var update: String by notNull()
        var delete: String by notNull()
        var dismiss: String by notNull()
        var retry: String by notNull()
        var loading: String by notNull()
        var noInternet: String by notNull()
        var apiError: String by notNull()

        // Products screen
        var addProduct: String by notNull()
        var scanBarcode: String by notNull()
        var noProductsYet: String by notNull()
        var productName: String by notNull()
        var barcodeOptional: String by notNull()
        var caloriesPer100g: String by notNull()
        var proteinPer100g: String by notNull()
        var carbsPer100g: String by notNull()
        var fatsPer100g: String by notNull()
        var nutritionalValuesPer100g: String by notNull()
        var addProductTitle: String by notNull()
        var editProductTitle: String by notNull()
        var productAlreadyExists: String by notNull()
        var productNotFound: String by notNull()
        var dataFromOpenFoodFacts: String by notNull()
        var visitOpenFoodFacts: String by notNull()
        var saveProduct: String by notNull()
        var updateProduct: String by notNull()

        // Meals screen
        var addMeal: String by notNull()
        var handsFree: String by notNull()
        var noMealsYet: String by notNull()
        var addMealTitle: String by notNull()
        var handsFreeMealTitle: String by notNull()
        var mealName: String by notNull()
        var products: String by notNull()
        var selectProduct: String by notNull()
        var quantityGrams: String by notNull()
        var addProductButton: String by notNull()
        var saveMeal: String by notNull()
        var addedProducts: String by notNull()
        var draftProducts: String by notNull()
        var resolve: String by notNull()
        var removeDraft: String by notNull()
        var noProductsAdded: String by notNull()
        var noDrafts: String by notNull()
        var resolveDraftTitle: String by notNull()
        var mapExisting: String by notNull()
        var createNew: String by notNull()
        var newProductName: String by notNull()
        var calories: String by notNull()
        var protein: String by notNull()
        var carbs: String by notNull()
        var fats: String by notNull()
        var totals: String by notNull()
        var per100g: String by notNull()
        var ingredients: String by notNull()
        var ingredientsPlural: String by notNull()
        var deleteMeal: String by notNull()
        var remove: String by notNull()
        var quantityLabel: String by notNull()

        // Voice recognition
        var ready: String by notNull()
        var microphonePermissionDenied: String by notNull()
        var listening: String by notNull()
        var speaking: String by notNull()
        var processing: String by notNull()
        var notListening: String by notNull()
        var commandNotRecognized: String by notNull()
        var noProductName: String by notNull()
        var pleaseSayQuantity: String by notNull()
        var addedProduct: String by notNull()
        var productNotFoundAddedDraft: String by notNull()
        var resolvedProduct: String by notNull()
        var couldNotUnderstand: String by notNull()
        var noCommandRecognized: String by notNull()
        var recognizerBusy: String by notNull()
        var errorWithCode: String by notNull()
        var resolved: String by notNull()
        var drafts: String by notNull()
        var pleaseEnterMealName: String by notNull()
        var addAtLeastOneProduct: String by notNull()
        var warning: String by notNull()
        var link: String by notNull()
        var speechPrompt: String by notNull()

        // Chat
        var aiAssistant: String by notNull()
        var aiAssistantLoading: String by notNull()
        var failedToLoadModel: String by notNull()
        var unknownError: String by notNull()
        var copy: String by notNull()
        var source: String by notNull()
        var askMeAnything: String by notNull()
        var send: String by notNull()
        var chatGreeting: String by notNull()
        var chatHelpGuide: String by notNull()
        var chatPromptForQuantities: String by notNull()
        var chatInvalidQuantityFormat: String by notNull()
        var chatNoDataForIngredients: String by notNull()
        var chatNutritionAssistantFallback: String by notNull()
        var chatErrorTemplate: String by notNull()
        var noReliableData: String by notNull()
        var addToMyProducts: String by notNull()
        var valuesPer100g: String by notNull()
        var openSource: String by notNull()
        var addToProducts: String by notNull()
        var copyMessage: String by notNull()
        var failedToGenerateResponse: String by notNull()
        var defaultFallbackResponse: String by notNull()

        // Barcode scanning
        var barcodeScanner: String by notNull()
        var lookingUpProduct: String by notNull()
        var noInternetConnection: String by notNull()
        var productAlreadyExistsSnack: String by notNull()
        var productNotFoundSnack: String by notNull()
        var apiErrorSnack: String by notNull()

        // Navigation / Footer
        var home: String by notNull()
        var tracking: String by notNull()
        var productsNav: String by notNull()
        var mealsNav: String by notNull()
        var settingsNav: String by notNull()
        var menu: String by notNull()
        var customizeFooter: String by notNull()
        var pages: String by notNull()
        var maximumPagesSelected: String by notNull()
        var quickActions: String by notNull()
        var selected: String by notNull()
        var notSelected: String by notNull()

        // Settings
        var settings: String by notNull()
        var profileSetup: String by notNull()
        var language: String by notNull()
        var selectLanguage: String by notNull()
        var english: String by notNull()
        var spanish: String by notNull()
        var french: String by notNull()
        var german: String by notNull()
        var romanian: String by notNull()

        // Home screen
        var welcomeBack: String by notNull()
        var userDefault: String by notNull()
        var userIcon: String by notNull()
        var steps: String by notNull()
        var stepsIcon: String by notNull()
        var remainingSteps: String by notNull()
        var stepSensorNotAvailable: String by notNull()
        var water: String by notNull()
        var waterIcon: String by notNull()
        var mlRemaining: String by notNull()
        var todaysIntake: String by notNull()
        var caloriesIcon: String by notNull()
        var noDataThisWeek: String by notNull()
        var analytics: String by notNull()
        var analyticsIcon: String by notNull()
        var weeklyCaloricIntake: String by notNull()
        var refreshIcon: String by notNull()
        var caloriesLeft: String by notNull()
        var exceededGoal: String by notNull()
        var moreCaloriesNeeded: String by notNull()
        var goalAchieved: String by notNull()
        var caloriesRemaining: String by notNull()
        var exceededBy: String by notNull()
        var perfect: String by notNull()
        var progressPercent: String by notNull()

        // Profile Setup tab
        var yourNutritionalProfile: String by notNull()
        var profileComplete: String by notNull()
        var personalInfo: String by notNull()
        var activityAndGoal: String by notNull()
        var ageLabel: String by notNull()
        var sexLabel: String by notNull()
        var heightLabel: String by notNull()
        var weightLabel: String by notNull()
        var activityLabel: String by notNull()
        var goalLabel: String by notNull()
        var years: String by notNull()
        var cm: String by notNull()
        var kg: String by notNull()
        var recommendedDailyCalories: String by notNull()
        var disclaimerText: String by notNull()
        var noProfileDataFound: String by notNull()
        var updateProfile: String by notNull()
        var setupProfile: String by notNull()

        // Activity level strings
        var sedentary: String by notNull()
        var lightExercise: String by notNull()
        var moderateExercise: String by notNull()
        var active: String by notNull()
        var veryActive: String by notNull()

        // Goal strings
        var extremeLoss: String by notNull()
        var moderateLoss: String by notNull()
        var maintain: String by notNull()
        var moderateGain: String by notNull()
        var extremeGain: String by notNull()

        // Macros screen
        var macros: String by notNull()
        var macrosFor: String by notNull()
        var kcalShort: String by notNull()
        var proteinShort: String by notNull()
        var carbsShort: String by notNull()
        var fatsShort: String by notNull()
        var amountLabel: String by notNull()
        var quantityLabelMeal: String by notNull()
        var ok: String by notNull()
        var afterAddingDeficit: String by notNull()
        var afterAddingExceeded: String by notNull()
        var afterAddingSurplus: String by notNull()
        var afterAddingGoalMet: String by notNull()
        var remainingToMaintain: String by notNull()
        var exceedMaintenance: String by notNull()
        var perfectMaintain: String by notNull()
        var effectOnGoal: String by notNull()
        var noGoalSet: String by notNull()
        var recommendSetGoal: String by notNull()
        var previewMacroIntake: String by notNull()
        var pleaseSelectProduct: String by notNull()
        var addToToday: String by notNull()
        var selectMeal: String by notNull()
        var mealContains: String by notNull()
        var totalMealWeight: String by notNull()
        var quantityConsumed: String by notNull()
        var servingFactor: String by notNull()
        var macroPreview: String by notNull()
        var pleaseSelectMeal: String by notNull()
        var amountGrams: String by notNull()

        // Steps screen
        var stepTracker: String by notNull()
        var stepsCount: String by notNull()
        var goalSteps: String by notNull()
        var percentCompleted: String by notNull()
        var insights: String by notNull()
        var lowActivity: String by notNull()
        var goodProgress: String by notNull()
        var greatJob: String by notNull()
        var excellentGoalAchieved: String by notNull()
        var remainingStepsLabel: String by notNull()
        var history: String by notNull()
        var historyPlaceholder: String by notNull()

        // UserDataSetupModal
        var basicInformation: String by notNull()
        var yourName: String by notNull()
        var ageYears: String by notNull()
        var sex: String by notNull()
        var male: String by notNull()
        var female: String by notNull()
        var heightCm: String by notNull()
        var weightKg: String by notNull()
        var activityLevelTitle: String by notNull()
        var howOftenExercise: String by notNull()
        var yourGoal: String by notNull()
        var baseMaintenance: String by notNull()
        var completePreviousSteps: String by notNull()
        var caloriesPerDayWeeklyChange: String by notNull()
        var goBackFillInfo: String by notNull()
        var back: String by notNull()
        var next: String by notNull()
        var calculate: String by notNull()

        // Water screen
        var totalWaterIntake: String by notNull()
        var mlValue: String by notNull()
        var editCupSizes: String by notNull()
        var recommendedIntake: String by notNull()
        var percentOfDailyGoal: String by notNull()
        var todaysHistory: String by notNull()
        var editCupSizesTitle: String by notNull()
        var cupLabel: String by notNull()
        var setRecommendedIntake: String by notNull()
        var recommendedMl: String by notNull()
        var notifications: String by notNull()

        // Utils / ProductNutritionLabel
        var nutritionPer100g: String by notNull()

        // MainActivity / Permissions
        var permissionsRequiredTitle: String by notNull()
        var permissionsRequiredDescription: String by notNull()
        var grantPermissions: String by notNull()

        // Camera / Barcode scanner
        var cameraPermissionNeeded: String by notNull()
        var grantPermission: String by notNull()
        var cameraPermissionDenied: String by notNull()
        var scanningProgress: String by notNull()
        var startingCamera: String by notNull()
        var errorPrefix: String by notNull()
        var positionBarcode: String by notNull()

        // Added: meals (for Macros screen button)
        var meals: String by notNull()
    }

    // ------------------------------------------------------------------
    // ENGLISH
    // ------------------------------------------------------------------
    private val englishStrings: Strings = Strings().apply {
        appName = "Salubris"
        close = "Close"
        cancel = "Cancel"
        confirm = "Confirm"
        save = "Save"
        update = "Update"
        delete = "Delete"
        dismiss = "Dismiss"
        retry = "Retry"
        loading = "Loading..."
        noInternet = "No internet connection"
        apiError = "API error – please try again"

        addProduct = "Add Product"
        scanBarcode = "Scan Barcode"
        noProductsYet = "No products yet"
        productName = "Product name"
        barcodeOptional = "Barcode (optional)"
        caloriesPer100g = "Calories (kcal)"
        proteinPer100g = "Protein (g)"
        carbsPer100g = "Carbs (g)"
        fatsPer100g = "Fats (g)"
        nutritionalValuesPer100g = "Nutritional values are per 100g"
        addProductTitle = "Add a product"
        editProductTitle = "Edit Product"
        productAlreadyExists = "Product already exists"
        productNotFound = "Product not found"
        dataFromOpenFoodFacts =
            "Data sourced from Open Food Facts. It may be outdated or incomplete."
        visitOpenFoodFacts = "Visit Open Food Facts"
        saveProduct = "Save Product"
        updateProduct = "Update Product"

        addMeal = "Add Meal"
        handsFree = "Hands‑free"
        noMealsYet = "No meals yet"
        addMealTitle = "Add a meal"
        handsFreeMealTitle = "Hands‑free Meal"
        mealName = "Meal name"
        products = "Products"
        selectProduct = "Select a product"
        quantityGrams = "Quantity (g)"
        addProductButton = "ADD PRODUCT"
        saveMeal = "Save Meal"
        addedProducts = "Added products"
        draftProducts = "Draft products"
        resolve = "Resolve"
        removeDraft = "Remove draft"
        noProductsAdded = "No products added yet"
        noDrafts = "No drafts"
        resolveDraftTitle = "Resolve draft: "
        mapExisting = "Map to existing product"
        createNew = "Create new product"
        newProductName = "Product name"
        calories = "Calories"
        protein = "Protein"
        carbs = "Carbs"
        fats = "Fats"
        totals = "Totals:"
        per100g = "per 100g"
        ingredients = "ingredient"
        ingredientsPlural = "ingredients"
        deleteMeal = "Delete meal"
        remove = "Remove"
        quantityLabel = "Quantity:"

        ready = "Ready"
        microphonePermissionDenied = "Microphone permission denied"
        listening = "Listening..."
        speaking = "Speaking..."
        processing = "Processing..."
        notListening = "Not listening"
        commandNotRecognized = "Command not recognized (start with 'add')"
        noProductName = "No product name"
        pleaseSayQuantity = "Please say quantity for {product} (e.g., '100 grams')"
        addedProduct = "Added {quantity}g of {product}"
        productNotFoundAddedDraft = "{product} not found – added as draft"
        resolvedProduct = "Resolved: {product}"
        couldNotUnderstand = "Could not understand"
        noCommandRecognized = "No command recognized"
        recognizerBusy = "Recognizer busy"
        errorWithCode = "Error: {error}"
        resolved = "Resolved"
        drafts = "Drafts"
        pleaseEnterMealName = "Please enter a meal name"
        addAtLeastOneProduct = "Add at least one product or draft"
        warning = "Warning"
        link = "Link"
        speechPrompt = "Say 'add product quantity'"

        aiAssistant = "AI Assistant"
        aiAssistantLoading = "Loading AI Assistant..."
        failedToLoadModel = "Failed to load model"
        unknownError = "Unknown error"
        copy = "Copy"
        source = "Source"
        askMeAnything = "Ask me anything..."
        send = "Send"
        chatGreeting =
            "👋 Hi! I'm your Salubris assistant. I search trusted nutrition sources so you don't have to. Ask me about macros, calories, or meal totals!\n\nType 'Help' for a guide and examples."
        chatHelpGuide =
            "🤖 **Salubris Nutrition Assistant**\n\nI search trusted sources (USDA, Open Food Facts, and the web) to give you accurate macronutrient data per 100 grams. I can also calculate meal totals when you provide quantities.\n\nHere's what you can ask me:\n\n• **Single food macros**\n   - \"macros for chicken breast per 100g\"\n   - \"calories in avocado 100 grams\"\n   - \"protein in salmon per 100g\"\n\n• **Meal calculation** (add quantities)\n   - \"200g chicken breast, 150g rice, 100g broccoli\"\n   - \"300g steak, 200g potatoes, 50g butter\"\n\n• **General nutrition & health questions**\n   - \"How much protein do I need per day?\"\n   - \"What are healthy sources of fat?\"\n   - \"Is intermittent fasting effective?\"\n\nJust type your question normally – I'll detect what you need and search the web for verified data. I never invent numbers."
        chatPromptForQuantities =
            "Please provide quantities for each food so I can calculate the meal macros.\n\nExample:\n200g chicken breast\n150g rice"
        chatInvalidQuantityFormat =
            "I couldn't understand the quantities. Please use the format: 200g chicken breast."
        chatNoDataForIngredients = "Could not find nutrition data for those ingredients."
        chatNutritionAssistantFallback =
            "🤔 I'm a nutrition assistant. I can help with food macros, meal calculations, and general nutrition questions.\n\nType 'Help' to see what I can do!"
        chatErrorTemplate = "❌ Something went wrong: %s"
        noReliableData = "🤔 I couldn't find reliable nutrition data for that food."
        addToMyProducts = "Add to My Products"
        valuesPer100g = "⚠️ Values are per 100 grams."
        openSource = "Open source"
        addToProducts = "Add to products"
        copyMessage = "Copy message"
        failedToGenerateResponse = "🤔 I couldn't generate a response."
        defaultFallbackResponse =
            "I'm sorry, I couldn't process that request. Please try asking about a specific food or type 'Help' for examples."

        barcodeScanner = "Barcode scanner"
        lookingUpProduct = "Looking up product..."
        noInternetConnection = "No internet connection"
        productAlreadyExistsSnack = "Product already exists"
        productNotFoundSnack = "Product not found in Open Food Facts"
        apiErrorSnack = "API error – please try again"

        home = "Home"
        tracking = "Tracking"
        productsNav = "Products"
        mealsNav = "Meals"
        settingsNav = "Settings"
        menu = "Menu"
        customizeFooter = "Customize Footer"
        pages = "Pages"
        maximumPagesSelected = "Maximum 4 pages selected"
        quickActions = "Quick Actions"
        selected = "Selected"
        notSelected = "Not selected"

        settings = "Settings"
        profileSetup = "Profile Setup"
        language = "Language"
        selectLanguage = "Select your language"
        english = "English"
        spanish = "Spanish"
        french = "French"
        german = "German"
        romanian = "Romanian"

        welcomeBack = "Welcome back"
        userDefault = "User"
        userIcon = "User icon"
        steps = "Steps"
        stepsIcon = "Steps"
        remainingSteps = "steps remaining"
        stepSensorNotAvailable = "Step sensor not available"
        water = "Water"
        waterIcon = "Water"
        mlRemaining = "ml remaining"
        todaysIntake = "Today's Intake"
        caloriesIcon = "Calories"
        noDataThisWeek = "No data this week"
        analytics = "Analytics"
        analyticsIcon = "Analytics"
        weeklyCaloricIntake = "Weekly caloric intake"
        refreshIcon = "Refresh"
        caloriesLeft = "🎯 %d calories left"
        exceededGoal = "⚠️ You've exceeded your goal"
        moreCaloriesNeeded = "💪 %d more calories needed"
        goalAchieved = "✅ Goal achieved!"
        caloriesRemaining = "%d calories remaining"
        exceededBy = "Exceeded by %d calories"
        perfect = "Perfect!"
        progressPercent = "Progress: %.1f%%"

        yourNutritionalProfile = "Your nutritional profile"
        profileComplete = "Profile complete"
        personalInfo = "Personal Info"
        activityAndGoal = "Activity & Goal"
        ageLabel = "Age"
        sexLabel = "Sex"
        heightLabel = "Height"
        weightLabel = "Weight"
        activityLabel = "Activity"
        goalLabel = "Goal"
        years = "years"
        cm = "cm"
        kg = "kg"
        recommendedDailyCalories = "Recommended daily calories: %d kcal"
        disclaimerText =
            "⚠️ Disclaimer: These are only recommendations. Consult a healthcare professional before making significant dietary changes."
        noProfileDataFound =
            "No profile data found. Click below to set up your age, sex, height, weight, goals and activity level."
        updateProfile = "Update profile"
        setupProfile = "Set up profile"

        sedentary = "Sedentary (little or no exercise)"
        lightExercise = "Light exercise (1-3 days/week)"
        moderateExercise = "Moderate exercise (3-5 days/week)"
        active = "Active (6-7 days/week)"
        veryActive = "Very active (hard daily exercise)"

        extremeLoss = "Extreme weight loss (1000 kcal deficit)"
        moderateLoss = "Moderate weight loss (500 kcal deficit)"
        maintain = "Maintain weight"
        moderateGain = "Moderate weight gain (500 kcal surplus)"
        extremeGain = "Extreme weight gain (1000 kcal surplus)"

        macros = "Macros"
        macrosFor = "Macros for "
        kcalShort = "Kcal"
        proteinShort = "Protein"
        carbsShort = "Carbs"
        fatsShort = "Fats"
        amountLabel = "Amount: %.1fg"
        quantityLabelMeal = "Quantity: %.1fg"
        ok = "OK"
        afterAddingDeficit = "After adding: 🎯 %d kcal left to stay in deficit"
        afterAddingExceeded = "⚠️ After adding: You will exceed your calorie goal"
        afterAddingSurplus = "After adding: 💪 %d more kcal needed to reach surplus"
        afterAddingGoalMet = "✅ After adding: You will meet or exceed your surplus goal"
        remainingToMaintain = "%d kcal remaining to maintain weight"
        exceedMaintenance = "Will exceed maintenance by %d kcal"
        perfectMaintain = "Perfect! You'll exactly hit your maintenance goal"
        effectOnGoal = "Effect on your daily goal"
        noGoalSet = "No goal set"
        recommendSetGoal =
            "We recommend that you set a main goal inside the settings section for a better experience"
        previewMacroIntake = "Preview macro intake"
        pleaseSelectProduct = "Please select a product to continue"
        addToToday = "Add to today"
        selectMeal = "Select a meal"
        mealContains = "Meal contains:"
        totalMealWeight = "Total meal weight: %sg"
        quantityConsumed = "Quantity consumed (g)"
        servingFactor = "Serving factor: %sx"
        macroPreview = "Macro preview"
        pleaseSelectMeal = "Please select a meal"
        amountGrams = "Amount (g)"

        stepTracker = "Step Tracker"
        stepsCount = "%d steps"
        goalSteps = "Goal: %d steps"
        percentCompleted = "%d%% completed"
        insights = "Insights"
        lowActivity = "Low activity — try taking a short walk."
        goodProgress = "Good progress — you're getting active."
        greatJob = "Great job — almost at your goal!"
        excellentGoalAchieved = "Excellent — goal achieved 🎉"
        remainingStepsLabel = "Remaining: %d steps"
        history = "History"
        historyPlaceholder = "Daily tracking history will appear here once persistence is added."

        basicInformation = "Basic information"
        yourName = "Your name"
        ageYears = "Age (years)"
        sex = "Sex"
        male = "Male"
        female = "Female"
        heightCm = "Height (cm)"
        weightKg = "Weight (kg)"
        activityLevelTitle = "Activity level"
        howOftenExercise = "How often do you exercise?"
        yourGoal = "Your goal"
        baseMaintenance = "Base maintenance: %d kcal/day"
        completePreviousSteps = "Complete previous steps first"
        caloriesPerDayWeeklyChange = "%d kcal/day · %s"
        goBackFillInfo = "Please go back and ensure all information is filled."
        back = "Back"
        next = "Next"
        calculate = "Calculate"

        totalWaterIntake = "Total Water Intake"
        mlValue = "%d ml"
        editCupSizes = "Edit cup sizes"
        recommendedIntake = "Recommended Intake"
        percentOfDailyGoal = "%d%% of daily goal"
        todaysHistory = "Today's History"
        editCupSizesTitle = "Edit Cup Sizes (ml)"
        cupLabel = "Cup %d"
        setRecommendedIntake = "Set Recommended Daily Intake (ml)"
        recommendedMl = "Recommended ml"
        notifications = "Notifications"

        nutritionPer100g = "Nutrition per 100g"

        permissionsRequiredTitle = "Permissions Required"
        permissionsRequiredDescription =
            "This app needs activity recognition and health service permissions to count your steps."
        grantPermissions = "Grant Permissions"

        cameraPermissionNeeded = "Camera permission is needed to scan barcodes"
        grantPermission = "Grant Permission"
        cameraPermissionDenied = "Camera permission denied"
        scanningProgress = "Scanning... (%d/3)"
        startingCamera = "Starting camera..."
        errorPrefix = "Error: "
        positionBarcode =
            "Position barcode inside the frame\nScanning multiple times for accuracy..."

        meals = "Meals"
    }

    // ------------------------------------------------------------------
    // SPANISH
    // ------------------------------------------------------------------
    private val spanishStrings: Strings = Strings().apply {
        appName = "Salubris"
        close = "Cerrar"
        cancel = "Cancelar"
        confirm = "Confirmar"
        save = "Guardar"
        update = "Actualizar"
        delete = "Eliminar"
        dismiss = "Descartar"
        retry = "Reintentar"
        loading = "Cargando..."
        noInternet = "Sin conexión a Internet"
        apiError = "Error de API – intente de nuevo"

        addProduct = "Añadir Producto"
        scanBarcode = "Escanear Código"
        noProductsYet = "Todavía no hay productos"
        productName = "Nombre del producto"
        barcodeOptional = "Código de barras (opcional)"
        caloriesPer100g = "Calorías (kcal)"
        proteinPer100g = "Proteínas (g)"
        carbsPer100g = "Carbohidratos (g)"
        fatsPer100g = "Grasas (g)"
        nutritionalValuesPer100g = "Valores nutricionales por 100g"
        addProductTitle = "Añadir un producto"
        editProductTitle = "Editar Producto"
        productAlreadyExists = "El producto ya existe"
        productNotFound = "Producto no encontrado"
        dataFromOpenFoodFacts =
            "Datos de Open Food Facts. Pueden estar desactualizados o incompletos."
        visitOpenFoodFacts = "Visitar Open Food Facts"
        saveProduct = "Guardar Producto"
        updateProduct = "Actualizar Producto"

        addMeal = "Añadir Comida"
        handsFree = "Manos libres"
        noMealsYet = "Todavía no hay comidas"
        addMealTitle = "Añadir una comida"
        handsFreeMealTitle = "Comida con manos libres"
        mealName = "Nombre de la comida"
        products = "Productos"
        selectProduct = "Seleccionar un producto"
        quantityGrams = "Cantidad (g)"
        addProductButton = "AÑADIR PRODUCTO"
        saveMeal = "Guardar Comida"
        addedProducts = "Productos añadidos"
        draftProducts = "Productos pendientes"
        resolve = "Resolver"
        removeDraft = "Eliminar pendiente"
        noProductsAdded = "Aún no se han añadido productos"
        noDrafts = "Sin pendientes"
        resolveDraftTitle = "Resolver pendiente: "
        mapExisting = "Vincular a producto existente"
        createNew = "Crear nuevo producto"
        newProductName = "Nombre del producto"
        calories = "Calorías"
        protein = "Proteínas"
        carbs = "Carbohidratos"
        fats = "Grasas"
        totals = "Totales:"
        per100g = "por 100g"
        ingredients = "ingrediente"
        ingredientsPlural = "ingredientes"
        deleteMeal = "Eliminar comida"
        remove = "Eliminar"
        quantityLabel = "Cantidad:"

        ready = "Listo"
        microphonePermissionDenied = "Permiso de micrófono denegado"
        listening = "Escuchando..."
        speaking = "Hablando..."
        processing = "Procesando..."
        notListening = "No escuchando"
        commandNotRecognized = "Comando no reconocido (empieza con 'add')"
        noProductName = "Sin nombre de producto"
        pleaseSayQuantity = "Por favor, indica la cantidad para {product} (ej. '100 gramos')"
        addedProduct = "Añadido {quantity}g de {product}"
        productNotFoundAddedDraft = "{product} no encontrado – añadido como pendiente"
        resolvedProduct = "Resuelto: {product}"
        couldNotUnderstand = "No se pudo entender"
        noCommandRecognized = "No se reconoció el comando"
        recognizerBusy = "Reconocedor ocupado"
        errorWithCode = "Error: {error}"
        resolved = "Resuelto"
        drafts = "Pendientes"
        pleaseEnterMealName = "Por favor, introduce un nombre para la comida"
        addAtLeastOneProduct = "Añade al menos un producto o pendiente"
        warning = "Advertencia"
        link = "Enlace"
        speechPrompt = "Di 'añadir producto cantidad'"

        aiAssistant = "Asistente IA"
        aiAssistantLoading = "Cargando asistente..."
        failedToLoadModel = "Error al cargar el modelo"
        unknownError = "Error desconocido"
        copy = "Copiar"
        source = "Fuente"
        askMeAnything = "Pregúntame cualquier cosa..."
        send = "Enviar"
        chatGreeting =
            "👋 ¡Hola! Soy tu asistente Salubris. Busco fuentes de nutrición confiables para que no tengas que hacerlo. ¡Pregúntame sobre macros, calorías o totales de comidas!\n\nEscribe 'Ayuda' para una guía y ejemplos."
        chatHelpGuide =
            "🤖 **Asistente de Nutrición Salubris**\n\nBusco fuentes confiables (USDA, Open Food Facts y la web) para darte datos precisos de macronutrientes por 100 gramos. También puedo calcular totales de comidas cuando proporcionas cantidades.\n\nEsto es lo que puedes preguntarme:\n\n• **Macros de un solo alimento**\n   - \"macros de pechuga de pollo por 100g\"\n   - \"calorías en aguacate 100 gramos\"\n   - \"proteína en salmón por 100g\"\n\n• **Cálculo de comida** (añade cantidades)\n   - \"200g pechuga de pollo, 150g arroz, 100g brócoli\"\n   - \"300g bistec, 200g patatas, 50g mantequilla\"\n\n• **Preguntas generales de nutrición y salud**\n   - \"¿Cuánta proteína necesito al día?\"\n   - \"¿Cuáles son las fuentes saludables de grasa?\"\n   - \"¿Es efectivo el ayuno intermitente?\"\n\nSolo escribe tu pregunta normalmente – detectaré lo que necesitas y buscaré en la web datos verificados. Nunca invento números."
        chatPromptForQuantities =
            "Por favor, proporciona cantidades para cada alimento para poder calcular los macros de la comida.\n\nEjemplo:\n200g pechuga de pollo\n150g arroz"
        chatInvalidQuantityFormat =
            "No pude entender las cantidades. Usa el formato: 200g pechuga de pollo."
        chatNoDataForIngredients = "No se encontraron datos nutricionales para esos ingredientes."
        chatNutritionAssistantFallback =
            "🤔 Soy un asistente de nutrición. Puedo ayudar con macros de alimentos, cálculos de comidas y preguntas generales de nutrición.\n\nEscribe 'Ayuda' para ver lo que puedo hacer."
        chatErrorTemplate = "❌ Algo salió mal: %s"
        noReliableData = "🤔 No pude encontrar datos nutricionales confiables para ese alimento."
        addToMyProducts = "Añadir a Mis Productos"
        valuesPer100g = "⚠️ Los valores son por 100 gramos."
        openSource = "Abrir fuente"
        addToProducts = "Añadir a productos"
        copyMessage = "Copiar mensaje"
        failedToGenerateResponse = "🤔 No pude generar una respuesta."
        defaultFallbackResponse =
            "Lo siento, no pude procesar esa solicitud. Por favor, intenta preguntando sobre un alimento específico o escribe 'Ayuda' para ejemplos."

        barcodeScanner = "Escaner de código"
        lookingUpProduct = "Buscando producto..."
        noInternetConnection = "Sin conexión a Internet"
        productAlreadyExistsSnack = "El producto ya existe"
        productNotFoundSnack = "Producto no encontrado en Open Food Facts"
        apiErrorSnack = "Error de API – intente de nuevo"

        home = "Inicio"
        tracking = "Seguimiento"
        productsNav = "Productos"
        mealsNav = "Comidas"
        settingsNav = "Ajustes"
        menu = "Menú"
        customizeFooter = "Personalizar Pie"
        pages = "Páginas"
        maximumPagesSelected = "Máximo 4 páginas seleccionadas"
        quickActions = "Acciones rápidas"
        selected = "Seleccionado"
        notSelected = "No seleccionado"

        settings = "Ajustes"
        profileSetup = "Configuración de perfil"
        language = "Idioma"
        selectLanguage = "Selecciona tu idioma"
        english = "Inglés"
        spanish = "Español"
        french = "Francés"
        german = "Alemán"
        romanian = "Rumano"

        welcomeBack = "Bienvenido de nuevo"
        userDefault = "Usuario"
        userIcon = "Icono de usuario"
        steps = "Pasos"
        stepsIcon = "Pasos"
        remainingSteps = "pasos restantes"
        stepSensorNotAvailable = "Sensor de pasos no disponible"
        water = "Agua"
        waterIcon = "Agua"
        mlRemaining = "ml restantes"
        todaysIntake = "Ingesta de hoy"
        caloriesIcon = "Calorías"
        noDataThisWeek = "Sin datos esta semana"
        analytics = "Analítica"
        analyticsIcon = "Analítica"
        weeklyCaloricIntake = "Ingesta calórica semanal"
        refreshIcon = "Refrescar"
        caloriesLeft = "🎯 %d calorías restantes"
        exceededGoal = "⚠️ Has excedido tu objetivo"
        moreCaloriesNeeded = "💪 %d calorías más necesarias"
        goalAchieved = "✅ ¡Objetivo alcanzado!"
        caloriesRemaining = "%d calorías restantes"
        exceededBy = "Excedido por %d calorías"
        perfect = "¡Perfecto!"
        progressPercent = "Progreso: %.1f%%"

        yourNutritionalProfile = "Tu perfil nutricional"
        profileComplete = "Perfil completo"
        personalInfo = "Información personal"
        activityAndGoal = "Actividad y Objetivo"
        ageLabel = "Edad"
        sexLabel = "Sexo"
        heightLabel = "Altura"
        weightLabel = "Peso"
        activityLabel = "Actividad"
        goalLabel = "Objetivo"
        years = "años"
        cm = "cm"
        kg = "kg"
        recommendedDailyCalories = "Calorías diarias recomendadas: %d kcal"
        disclaimerText =
            "⚠️ Aviso: Estas son solo recomendaciones. Consulta a un profesional de la salud antes de hacer cambios significativos en la dieta."
        noProfileDataFound =
            "No se encontraron datos de perfil. Haz clic abajo para configurar tu edad, sexo, altura, peso, objetivos y nivel de actividad."
        updateProfile = "Actualizar perfil"
        setupProfile = "Configurar perfil"

        sedentary = "Sedentario (poco o nada de ejercicio)"
        lightExercise = "Ejercicio ligero (1-3 días/semana)"
        moderateExercise = "Ejercicio moderado (3-5 días/semana)"
        active = "Activo (6-7 días/semana)"
        veryActive = "Muy activo (ejercicio intenso diario)"

        extremeLoss = "Pérdida de peso extrema (déficit de 1000 kcal)"
        moderateLoss = "Pérdida de peso moderada (déficit de 500 kcal)"
        maintain = "Mantener peso"
        moderateGain = "Aumento de peso moderado (superávit de 500 kcal)"
        extremeGain = "Aumento de peso extremo (superávit de 1000 kcal)"

        macros = "Macros"
        macrosFor = "Macros para "
        kcalShort = "Kcal"
        proteinShort = "Proteína"
        carbsShort = "Carbohidratos"
        fatsShort = "Grasas"
        amountLabel = "Cantidad: %.1fg"
        quantityLabelMeal = "Cantidad: %.1fg"
        ok = "Aceptar"
        afterAddingDeficit = "Después de añadir: 🎯 %d kcal restantes para mantener el déficit"
        afterAddingExceeded = "⚠️ Después de añadir: Excederás tu objetivo calórico"
        afterAddingSurplus =
            "Después de añadir: 💪 %d kcal más necesarias para alcanzar el superávit"
        afterAddingGoalMet = "✅ Después de añadir: Alcanzarás o superarás tu objetivo de superávit"
        remainingToMaintain = "%d kcal restantes para mantener peso"
        exceedMaintenance = "Excederás el mantenimiento en %d kcal"
        perfectMaintain = "¡Perfecto! Alcanzarás exactamente tu objetivo de mantenimiento"
        effectOnGoal = "Efecto sobre tu objetivo diario"
        noGoalSet = "Sin objetivo establecido"
        recommendSetGoal =
            "Recomendamos que establezcas un objetivo principal en la sección de ajustes para una mejor experiencia"
        previewMacroIntake = "Vista previa de ingesta de macros"
        pleaseSelectProduct = "Selecciona un producto para continuar"
        addToToday = "Añadir a hoy"
        selectMeal = "Selecciona una comida"
        mealContains = "La comida contiene:"
        totalMealWeight = "Peso total de la comida: %sg"
        quantityConsumed = "Cantidad consumida (g)"
        servingFactor = "Factor de porción: %sx"
        macroPreview = "Vista previa de macros"
        pleaseSelectMeal = "Selecciona una comida"
        amountGrams = "Cantidad (g)"

        stepTracker = "Seguimiento de Pasos"
        stepsCount = "%d pasos"
        goalSteps = "Objetivo: %d pasos"
        percentCompleted = "%d%% completado"
        insights = "Información"
        lowActivity = "Actividad baja – intenta dar un paseo corto."
        goodProgress = "Buen progreso – te estás moviendo."
        greatJob = "¡Buen trabajo – casi llegas a tu objetivo!"
        excellentGoalAchieved = "¡Excelente – objetivo alcanzado 🎉"
        remainingStepsLabel = "Restantes: %d pasos"
        history = "Historial"
        historyPlaceholder =
            "El historial de seguimiento diario aparecerá aquí una vez que se añada persistencia."

        basicInformation = "Información básica"
        yourName = "Tu nombre"
        ageYears = "Edad (años)"
        sex = "Sexo"
        male = "Masculino"
        female = "Femenino"
        heightCm = "Altura (cm)"
        weightKg = "Peso (kg)"
        activityLevelTitle = "Nivel de actividad"
        howOftenExercise = "¿Con qué frecuencia haces ejercicio?"
        yourGoal = "Tu objetivo"
        baseMaintenance = "Mantenimiento base: %d kcal/día"
        completePreviousSteps = "Completa los pasos anteriores primero"
        caloriesPerDayWeeklyChange = "%d kcal/día · %s"
        goBackFillInfo = "Vuelve atrás y asegúrate de que toda la información está rellena."
        back = "Atrás"
        next = "Siguiente"
        calculate = "Calcular"

        totalWaterIntake = "Ingesta total de agua"
        mlValue = "%d ml"
        editCupSizes = "Editar tamaños de vaso"
        recommendedIntake = "Ingesta recomendada"
        percentOfDailyGoal = "%d%% del objetivo diario"
        todaysHistory = "Historial de hoy"
        editCupSizesTitle = "Editar Tamaños de Vaso (ml)"
        cupLabel = "Vaso %d"
        setRecommendedIntake = "Establecer Ingesta Diaria Recomendada (ml)"
        recommendedMl = "ml recomendados"
        notifications = "Notificaciones"

        nutritionPer100g = "Nutrición por 100g"

        permissionsRequiredTitle = "Permisos necesarios"
        permissionsRequiredDescription =
            "Esta aplicación necesita permisos de reconocimiento de actividad y servicio de salud para contar tus pasos."
        grantPermissions = "Conceder permisos"

        cameraPermissionNeeded = "Se necesita permiso de cámara para escanear códigos de barras"
        grantPermission = "Conceder permiso"
        cameraPermissionDenied = "Permiso de cámara denegado"
        scanningProgress = "Escaneando... (%d/3)"
        startingCamera = "Iniciando cámara..."
        errorPrefix = "Error: "
        positionBarcode =
            "Coloca el código de barras dentro del marco\nEscaneando varias veces para mayor precisión..."

        meals = "Comidas"
    }

    // ------------------------------------------------------------------
    // FRENCH
    // ------------------------------------------------------------------
    private val frenchStrings: Strings = Strings().apply {
        appName = "Salubris"
        close = "Fermer"
        cancel = "Annuler"
        confirm = "Confirmer"
        save = "Enregistrer"
        update = "Mettre à jour"
        delete = "Supprimer"
        dismiss = "Ignorer"
        retry = "Réessayer"
        loading = "Chargement..."
        noInternet = "Pas de connexion Internet"
        apiError = "Erreur API – veuillez réessayer"

        addProduct = "Ajouter un produit"
        scanBarcode = "Scanner un code‑barres"
        noProductsYet = "Aucun produit pour l'instant"
        productName = "Nom du produit"
        barcodeOptional = "Code‑barres (facultatif)"
        caloriesPer100g = "Calories (kcal)"
        proteinPer100g = "Protéines (g)"
        carbsPer100g = "Glucides (g)"
        fatsPer100g = "Lipides (g)"
        nutritionalValuesPer100g = "Valeurs nutritionnelles pour 100g"
        addProductTitle = "Ajouter un produit"
        editProductTitle = "Modifier le produit"
        productAlreadyExists = "Le produit existe déjà"
        productNotFound = "Produit non trouvé"
        dataFromOpenFoodFacts =
            "Données issues d'Open Food Facts. Elles peuvent être obsolètes ou incomplètes."
        visitOpenFoodFacts = "Visiter Open Food Facts"
        saveProduct = "Enregistrer le produit"
        updateProduct = "Mettre à jour le produit"

        addMeal = "Ajouter un repas"
        handsFree = "Mains libres"
        noMealsYet = "Aucun repas pour l'instant"
        addMealTitle = "Ajouter un repas"
        handsFreeMealTitle = "Repas mains libres"
        mealName = "Nom du repas"
        products = "Produits"
        selectProduct = "Sélectionner un produit"
        quantityGrams = "Quantité (g)"
        addProductButton = "AJOUTER LE PRODUIT"
        saveMeal = "Enregistrer le repas"
        addedProducts = "Produits ajoutés"
        draftProducts = "Produits en attente"
        resolve = "Résoudre"
        removeDraft = "Supprimer le brouillon"
        noProductsAdded = "Aucun produit ajouté"
        noDrafts = "Aucun brouillon"
        resolveDraftTitle = "Résoudre le brouillon : "
        mapExisting = "Associer à un produit existant"
        createNew = "Créer un nouveau produit"
        newProductName = "Nom du produit"
        calories = "Calories"
        protein = "Protéines"
        carbs = "Glucides"
        fats = "Lipides"
        totals = "Totaux :"
        per100g = "pour 100g"
        ingredients = "ingrédient"
        ingredientsPlural = "ingrédients"
        deleteMeal = "Supprimer le repas"
        remove = "Supprimer"
        quantityLabel = "Quantité :"

        ready = "Prêt"
        microphonePermissionDenied = "Permission du microphone refusée"
        listening = "Écoute..."
        speaking = "Parle..."
        processing = "Traitement..."
        notListening = "N'écoute pas"
        commandNotRecognized = "Commande non reconnue (commencez par 'add')"
        noProductName = "Pas de nom de produit"
        pleaseSayQuantity = "Veuillez indiquer la quantité pour {product} (ex. '100 grammes')"
        addedProduct = "Ajouté {quantity}g de {product}"
        productNotFoundAddedDraft = "{product} introuvable – ajouté comme brouillon"
        resolvedProduct = "Résolu : {product}"
        couldNotUnderstand = "Je n'ai pas compris"
        noCommandRecognized = "Aucune commande reconnue"
        recognizerBusy = "Reconnaissance occupée"
        errorWithCode = "Erreur : {error}"
        resolved = "Résolu"
        drafts = "Brouillons"
        pleaseEnterMealName = "Veuillez entrer un nom pour le repas"
        addAtLeastOneProduct = "Ajoutez au moins un produit ou un brouillon"
        warning = "Avertissement"
        link = "Lien"
        speechPrompt = "Dites 'ajouter produit quantité'"

        aiAssistant = "Assistant IA"
        aiAssistantLoading = "Chargement de l'assistant..."
        failedToLoadModel = "Échec du chargement du modèle"
        unknownError = "Erreur inconnue"
        copy = "Copier"
        source = "Source"
        askMeAnything = "Demandez‑moi n'importe quoi..."
        send = "Envoyer"
        chatGreeting =
            "👋 Bonjour ! Je suis votre assistant Salubris. Je recherche des sources nutritionnelles fiables pour que vous n'ayez pas à le faire. Posez‑moi des questions sur les macros, les calories ou les totaux de repas !\n\nTapez 'Aide' pour un guide et des exemples."
        chatHelpGuide =
            "🤖 **Assistant Nutrition Salubris**\n\nJe recherche des sources fiables (USDA, Open Food Facts et le web) pour vous fournir des données précises de macronutriments pour 100 grammes. Je peux aussi calculer les totaux des repas lorsque vous fournissez les quantités.\n\nVoici ce que vous pouvez me demander :\n\n• **Macros d'un seul aliment**\n   - \"macros pour blanc de poulet pour 100g\"\n   - \"calories dans l'avocat 100 grammes\"\n   - \"protéines dans le saumon pour 100g\"\n\n• **Calcul de repas** (ajoutez les quantités)\n   - \"200g de blanc de poulet, 150g de riz, 100g de brocoli\"\n   - \"300g de steak, 200g de pommes de terre, 50g de beurre\"\n\n• **Questions générales sur la nutrition et la santé**\n   - \"De combien de protéines ai‑je besoin par jour ?\"\n   - \"Quelles sont les sources saines de matières grasses ?\"\n   - \"Le jeûne intermittent est‑il efficace ?\"\n\nTapez votre question normalement – je détecterai ce dont vous avez besoin et chercherai sur le web des données vérifiées. Je n'invente jamais de chiffres."
        chatPromptForQuantities =
            "Veuillez fournir les quantités pour chaque aliment afin que je puisse calculer les macros du repas.\n\nExemple :\n200g de blanc de poulet\n150g de riz"
        chatInvalidQuantityFormat =
            "Je n'ai pas compris les quantités. Veuillez utiliser le format : 200g de blanc de poulet."
        chatNoDataForIngredients =
            "Impossible de trouver des données nutritionnelles pour ces ingrédients."
        chatNutritionAssistantFallback =
            "🤔 Je suis un assistant nutritionnel. Je peux aider avec les macros des aliments, les calculs de repas et les questions générales sur la nutrition.\n\nTapez 'Aide' pour voir ce que je peux faire."
        chatErrorTemplate = "❌ Quelque chose s'est mal passé : %s"
        noReliableData = "🤔 Je n'ai pas trouvé de données nutritionnelles fiables pour cet aliment."
        addToMyProducts = "Ajouter à Mes Produits"
        valuesPer100g = "⚠️ Les valeurs sont pour 100 grammes."
        openSource = "Ouvrir la source"
        addToProducts = "Ajouter aux produits"
        copyMessage = "Copier le message"
        failedToGenerateResponse = "🤔 Je n'ai pas pu générer une réponse."
        defaultFallbackResponse =
            "Je suis désolé, je n'ai pas pu traiter cette demande. Veuillez essayer de poser une question sur un aliment spécifique ou tapez 'Aide' pour des exemples."

        barcodeScanner = "Scanner de code‑barres"
        lookingUpProduct = "Recherche du produit..."
        noInternetConnection = "Pas de connexion Internet"
        productAlreadyExistsSnack = "Le produit existe déjà"
        productNotFoundSnack = "Produit non trouvé dans Open Food Facts"
        apiErrorSnack = "Erreur API – veuillez réessayer"

        home = "Accueil"
        tracking = "Suivi"
        productsNav = "Produits"
        mealsNav = "Repas"
        settingsNav = "Paramètres"
        menu = "Menu"
        customizeFooter = "Personnaliser le pied de page"
        pages = "Pages"
        maximumPagesSelected = "Maximum 4 pages sélectionnées"
        quickActions = "Actions rapides"
        selected = "Sélectionné"
        notSelected = "Non sélectionné"

        settings = "Paramètres"
        profileSetup = "Configuration du profil"
        language = "Langue"
        selectLanguage = "Choisissez votre langue"
        english = "Anglais"
        spanish = "Espagnol"
        french = "Français"
        german = "Allemand"
        romanian = "Roumain"

        welcomeBack = "Bon retour"
        userDefault = "Utilisateur"
        userIcon = "Icône utilisateur"
        steps = "Pas"
        stepsIcon = "Pas"
        remainingSteps = "pas restants"
        stepSensorNotAvailable = "Capteur de pas non disponible"
        water = "Eau"
        waterIcon = "Eau"
        mlRemaining = "ml restants"
        todaysIntake = "Apport du jour"
        caloriesIcon = "Calories"
        noDataThisWeek = "Aucune donnée cette semaine"
        analytics = "Analytiques"
        analyticsIcon = "Analytiques"
        weeklyCaloricIntake = "Apport calorique hebdomadaire"
        refreshIcon = "Rafraîchir"
        caloriesLeft = "🎯 %d calories restantes"
        exceededGoal = "⚠️ Vous avez dépassé votre objectif"
        moreCaloriesNeeded = "💪 %d calories supplémentaires nécessaires"
        goalAchieved = "✅ Objectif atteint !"
        caloriesRemaining = "%d calories restantes"
        exceededBy = "Dépassé de %d calories"
        perfect = "Parfait !"
        progressPercent = "Progression : %.1f%%"

        yourNutritionalProfile = "Votre profil nutritionnel"
        profileComplete = "Profil complet"
        personalInfo = "Informations personnelles"
        activityAndGoal = "Activité et Objectif"
        ageLabel = "Âge"
        sexLabel = "Sexe"
        heightLabel = "Taille"
        weightLabel = "Poids"
        activityLabel = "Activité"
        goalLabel = "Objectif"
        years = "ans"
        cm = "cm"
        kg = "kg"
        recommendedDailyCalories = "Calories quotidiennes recommandées : %d kcal"
        disclaimerText =
            "⚠️ Avertissement : Ce ne sont que des recommandations. Consultez un professionnel de la santé avant d'apporter des modifications importantes à votre alimentation."
        noProfileDataFound =
            "Aucune donnée de profil trouvée. Cliquez ci‑dessous pour configurer votre âge, votre sexe, votre taille, votre poids, vos objectifs et votre niveau d'activité."
        updateProfile = "Mettre à jour le profil"
        setupProfile = "Configurer le profil"

        sedentary = "Sédentaire (peu ou pas d'exercice)"
        lightExercise = "Exercice léger (1-3 jours/semaine)"
        moderateExercise = "Exercice modéré (3-5 jours/semaine)"
        active = "Actif (6-7 jours/semaine)"
        veryActive = "Très actif (exercice intense quotidien)"

        extremeLoss = "Perte de poids extrême (déficit de 1000 kcal)"
        moderateLoss = "Perte de poids modérée (déficit de 500 kcal)"
        maintain = "Maintenir le poids"
        moderateGain = "Prise de poids modérée (surplus de 500 kcal)"
        extremeGain = "Prise de poids extrême (surplus de 1000 kcal)"

        macros = "Macros"
        macrosFor = "Macros pour "
        kcalShort = "Kcal"
        proteinShort = "Protéines"
        carbsShort = "Glucides"
        fatsShort = "Lipides"
        amountLabel = "Quantité : %.1fg"
        quantityLabelMeal = "Quantité : %.1fg"
        ok = "OK"
        afterAddingDeficit = "Après ajout : 🎯 %d kcal restantes pour rester en déficit"
        afterAddingExceeded = "⚠️ Après ajout : Vous dépasserez votre objectif calorique"
        afterAddingSurplus =
            "Après ajout : 💪 %d kcal supplémentaires nécessaires pour atteindre le surplus"
        afterAddingGoalMet =
            "✅ Après ajout : Vous atteindrez ou dépasserez votre objectif de surplus"
        remainingToMaintain = "%d kcal restantes pour maintenir le poids"
        exceedMaintenance = "Dépassement de l'entretien de %d kcal"
        perfectMaintain = "Parfait ! Vous atteindrez exactement votre objectif de maintien"
        effectOnGoal = "Effet sur votre objectif quotidien"
        noGoalSet = "Aucun objectif défini"
        recommendSetGoal =
            "Nous vous recommandons de définir un objectif principal dans la section des paramètres pour une meilleure expérience"
        previewMacroIntake = "Aperçu de l'apport macro"
        pleaseSelectProduct = "Veuillez sélectionner un produit pour continuer"
        addToToday = "Ajouter à aujourd'hui"
        selectMeal = "Sélectionner un repas"
        mealContains = "Le repas contient :"
        totalMealWeight = "Poids total du repas : %sg"
        quantityConsumed = "Quantité consommée (g)"
        servingFactor = "Facteur de portion : %sx"
        macroPreview = "Aperçu des macros"
        pleaseSelectMeal = "Veuillez sélectionner un repas"
        amountGrams = "Quantité (g)"

        stepTracker = "Suivi des pas"
        stepsCount = "%d pas"
        goalSteps = "Objectif : %d pas"
        percentCompleted = "%d%% effectué"
        insights = "Aperçus"
        lowActivity = "Faible activité – essayez de faire une courte promenade."
        goodProgress = "Bon progrès – vous devenez actif."
        greatJob = "Bon travail – presque à votre objectif !"
        excellentGoalAchieved = "Excellent – objectif atteint 🎉"
        remainingStepsLabel = "Restants : %d pas"
        history = "Historique"
        historyPlaceholder =
            "L'historique de suivi quotidien apparaîtra ici une fois la persistance ajoutée."

        basicInformation = "Informations de base"
        yourName = "Votre nom"
        ageYears = "Âge (ans)"
        sex = "Sexe"
        male = "Masculin"
        female = "Féminin"
        heightCm = "Taille (cm)"
        weightKg = "Poids (kg)"
        activityLevelTitle = "Niveau d'activité"
        howOftenExercise = "À quelle fréquence faites-vous de l'exercice ?"
        yourGoal = "Votre objectif"
        baseMaintenance = "Entretien de base : %d kcal/jour"
        completePreviousSteps = "Veuillez d'abord compléter les étapes précédentes"
        caloriesPerDayWeeklyChange = "%d kcal/jour · %s"
        goBackFillInfo =
            "Veuillez revenir en arrière et vous assurer que toutes les informations sont remplies."
        back = "Retour"
        next = "Suivant"
        calculate = "Calculer"

        totalWaterIntake = "Apport total en eau"
        mlValue = "%d ml"
        editCupSizes = "Modifier les tailles de verre"
        recommendedIntake = "Apport recommandé"
        percentOfDailyGoal = "%d%% de l'objectif quotidien"
        todaysHistory = "Historique du jour"
        editCupSizesTitle = "Modifier les Tailles de Verre (ml)"
        cupLabel = "Verre %d"
        setRecommendedIntake = "Définir l'apport quotidien recommandé (ml)"
        recommendedMl = "ml recommandés"
        notifications = "Notifications"

        nutritionPer100g = "Nutrition pour 100g"

        permissionsRequiredTitle = "Permissions requises"
        permissionsRequiredDescription =
            "Cette application a besoin des permissions de reconnaissance d'activité et de service de santé pour compter vos pas."
        grantPermissions = "Accorder les permissions"

        cameraPermissionNeeded =
            "La permission de la caméra est nécessaire pour scanner les codes‑barres"
        grantPermission = "Accorder la permission"
        cameraPermissionDenied = "Permission de la caméra refusée"
        scanningProgress = "Scan en cours... (%d/3)"
        startingCamera = "Démarrage de la caméra..."
        errorPrefix = "Erreur : "
        positionBarcode =
            "Placez le code‑barres dans le cadre\nScan multiple pour plus de précision..."

        meals = "Repas"
    }

    // ------------------------------------------------------------------
    // GERMAN
    // ------------------------------------------------------------------
    private val germanStrings: Strings = Strings().apply {
        appName = "Salubris"
        close = "Schließen"
        cancel = "Abbrechen"
        confirm = "Bestätigen"
        save = "Speichern"
        update = "Aktualisieren"
        delete = "Löschen"
        dismiss = "Verwerfen"
        retry = "Wiederholen"
        loading = "Laden..."
        noInternet = "Keine Internetverbindung"
        apiError = "API-Fehler – bitte versuchen Sie es erneut"

        addProduct = "Produkt hinzufügen"
        scanBarcode = "Barcode scannen"
        noProductsYet = "Noch keine Produkte"
        productName = "Produktname"
        barcodeOptional = "Barcode (optional)"
        caloriesPer100g = "Kalorien (kcal)"
        proteinPer100g = "Protein (g)"
        carbsPer100g = "Kohlenhydrate (g)"
        fatsPer100g = "Fett (g)"
        nutritionalValuesPer100g = "Nährwerte pro 100g"
        addProductTitle = "Ein Produkt hinzufügen"
        editProductTitle = "Produkt bearbeiten"
        productAlreadyExists = "Produkt existiert bereits"
        productNotFound = "Produkt nicht gefunden"
        dataFromOpenFoodFacts =
            "Daten von Open Food Facts. Sie können veraltet oder unvollständig sein."
        visitOpenFoodFacts = "Open Food Facts besuchen"
        saveProduct = "Produkt speichern"
        updateProduct = "Produkt aktualisieren"

        addMeal = "Mahlzeit hinzufügen"
        handsFree = "Freihändig"
        noMealsYet = "Noch keine Mahlzeiten"
        addMealTitle = "Eine Mahlzeit hinzufügen"
        handsFreeMealTitle = "Freihändige Mahlzeit"
        mealName = "Name der Mahlzeit"
        products = "Produkte"
        selectProduct = "Ein Produkt auswählen"
        quantityGrams = "Menge (g)"
        addProductButton = "PRODUKT HINZUFÜGEN"
        saveMeal = "Mahlzeit speichern"
        addedProducts = "Hinzugefügte Produkte"
        draftProducts = "Entwürfe"
        resolve = "Auflösen"
        removeDraft = "Entwurf entfernen"
        noProductsAdded = "Noch keine Produkte hinzugefügt"
        noDrafts = "Keine Entwürfe"
        resolveDraftTitle = "Entwurf auflösen: "
        mapExisting = "Vorhandenem Produkt zuordnen"
        createNew = "Neues Produkt erstellen"
        newProductName = "Produktname"
        calories = "Kalorien"
        protein = "Protein"
        carbs = "Kohlenhydrate"
        fats = "Fett"
        totals = "Gesamt:"
        per100g = "pro 100g"
        ingredients = "Zutat"
        ingredientsPlural = "Zutaten"
        deleteMeal = "Mahlzeit löschen"
        remove = "Entfernen"
        quantityLabel = "Menge:"

        ready = "Bereit"
        microphonePermissionDenied = "Mikrofonberechtigung verweigert"
        listening = "Hört zu..."
        speaking = "Spricht..."
        processing = "Verarbeitet..."
        notListening = "Hört nicht zu"
        commandNotRecognized = "Befehl nicht erkannt (beginnen Sie mit 'add')"
        noProductName = "Kein Produktname"
        pleaseSayQuantity = "Bitte geben Sie die Menge für {product} an (z.B. '100 Gramm')"
        addedProduct = "{quantity}g von {product} hinzugefügt"
        productNotFoundAddedDraft = "{product} nicht gefunden – als Entwurf hinzugefügt"
        resolvedProduct = "Aufgelöst: {product}"
        couldNotUnderstand = "Konnte nicht verstehen"
        noCommandRecognized = "Kein Befehl erkannt"
        recognizerBusy = "Erkennung beschäftigt"
        errorWithCode = "Fehler: {error}"
        resolved = "Aufgelöst"
        drafts = "Entwürfe"
        pleaseEnterMealName = "Bitte geben Sie einen Namen für die Mahlzeit ein"
        addAtLeastOneProduct = "Fügen Sie mindestens ein Produkt oder einen Entwurf hinzu"
        warning = "Warnung"
        link = "Link"
        speechPrompt = "Sagen Sie 'Produkt Menge hinzufügen'"

        aiAssistant = "KI-Assistent"
        aiAssistantLoading = "Lade KI-Assistent..."
        failedToLoadModel = "Fehler beim Laden des Modells"
        unknownError = "Unbekannter Fehler"
        copy = "Kopieren"
        source = "Quelle"
        askMeAnything = "Fragen Sie mich alles..."
        send = "Senden"
        chatGreeting =
            "👋 Hallo! Ich bin Ihr Salubris-Assistent. Ich durchsuche vertrauenswürdige Ernährungsquellen, damit Sie es nicht tun müssen. Fragen Sie mich nach Makros, Kalorien oder Mahlzeiten-Summen!\n\nGeben Sie 'Hilfe' für eine Anleitung und Beispiele ein."
        chatHelpGuide =
            "🤖 **Salubris Ernährung Assistent**\n\nIch durchsuche vertrauenswürdige Quellen (USDA, Open Food Facts und das Web), um Ihnen genaue Makronährstoffdaten pro 100 Gramm zu liefern. Ich kann auch Mahlzeiten-Summen berechnen, wenn Sie Mengen angeben.\n\nHier ist, was Sie mich fragen können:\n\n• **Makros eines einzelnen Lebensmittels**\n   - \"Makros für Hähnchenbrust pro 100g\"\n   - \"Kalorien in Avocado 100 Gramm\"\n   - \"Protein in Lachs pro 100g\"\n\n• **Mahlzeitenberechnung** (Mengen angeben)\n   - \"200g Hähnchenbrust, 150g Reis, 100g Brokkoli\"\n   - \"300g Steak, 200g Kartoffeln, 50g Butter\"\n\n• **Allgemeine Ernährungs- und Gesundheitsfragen**\n   - \"Wie viel Protein brauche ich pro Tag?\"\n   - \"Was sind gesunde Fettquellen?\"\n   - \"Ist intermittierendes Fasten effektiv?\"\n\nGeben Sie Ihre Frage einfach normal ein – ich erkenne, was Sie brauchen, und suche im Web nach verifizierten Daten. Ich erfinde niemals Zahlen."
        chatPromptForQuantities =
            "Bitte geben Sie Mengen für jedes Lebensmittel an, damit ich die Mahlzeiten-Makros berechnen kann.\n\nBeispiel:\n200g Hähnchenbrust\n150g Reis"
        chatInvalidQuantityFormat =
            "Ich konnte die Mengen nicht verstehen. Bitte verwenden Sie das Format: 200g Hähnchenbrust."
        chatNoDataForIngredients = "Keine Nährstoffdaten für diese Zutaten gefunden."
        chatNutritionAssistantFallback =
            "🤔 Ich bin ein Ernährungsassistent. Ich kann bei Lebensmittel-Makros, Mahlzeitenberechnungen und allgemeinen Ernährungsfragen helfen.\n\nGeben Sie 'Hilfe' ein, um zu sehen, was ich tun kann."
        chatErrorTemplate = "❌ Etwas ist schiefgelaufen: %s"
        noReliableData =
            "🤔 Ich konnte keine zuverlässigen Nährstoffdaten für dieses Lebensmittel finden."
        addToMyProducts = "Zu meinen Produkten hinzufügen"
        valuesPer100g = "⚠️ Werte sind pro 100 Gramm."
        openSource = "Quelle öffnen"
        addToProducts = "Zu Produkten hinzufügen"
        copyMessage = "Nachricht kopieren"
        failedToGenerateResponse = "🤔 Ich konnte keine Antwort generieren."
        defaultFallbackResponse =
            "Es tut mir leid, ich konnte diese Anfrage nicht verarbeiten. Bitte versuchen Sie, nach einem bestimmten Lebensmittel zu fragen, oder geben Sie 'Hilfe' für Beispiele ein."

        barcodeScanner = "Barcode-Scanner"
        lookingUpProduct = "Produkt wird gesucht..."
        noInternetConnection = "Keine Internetverbindung"
        productAlreadyExistsSnack = "Produkt existiert bereits"
        productNotFoundSnack = "Produkt nicht in Open Food Facts gefunden"
        apiErrorSnack = "API-Fehler – bitte versuchen Sie es erneut"

        home = "Startseite"
        tracking = "Tracking"
        productsNav = "Produkte"
        mealsNav = "Mahlzeiten"
        settingsNav = "Einstellungen"
        menu = "Menü"
        customizeFooter = "Fußzeile anpassen"
        pages = "Seiten"
        maximumPagesSelected = "Maximal 4 Seiten ausgewählt"
        quickActions = "Schnellaktionen"
        selected = "Ausgewählt"
        notSelected = "Nicht ausgewählt"

        settings = "Einstellungen"
        profileSetup = "Profil einrichten"
        language = "Sprache"
        selectLanguage = "Wählen Sie Ihre Sprache"
        english = "Englisch"
        spanish = "Spanisch"
        french = "Französisch"
        german = "Deutsch"
        romanian = "Rumänisch"

        welcomeBack = "Willkommen zurück"
        userDefault = "Benutzer"
        userIcon = "Benutzersymbol"
        steps = "Schritte"
        stepsIcon = "Schritte"
        remainingSteps = "Schritte verbleibend"
        stepSensorNotAvailable = "Schrittsensor nicht verfügbar"
        water = "Wasser"
        waterIcon = "Wasser"
        mlRemaining = "ml verbleibend"
        todaysIntake = "Heutige Aufnahme"
        caloriesIcon = "Kalorien"
        noDataThisWeek = "Keine Daten diese Woche"
        analytics = "Analysen"
        analyticsIcon = "Analysen"
        weeklyCaloricIntake = "Wöchentliche Kalorienaufnahme"
        refreshIcon = "Aktualisieren"
        caloriesLeft = "🎯 %d Kalorien übrig"
        exceededGoal = "⚠️ Sie haben Ihr Ziel überschritten"
        moreCaloriesNeeded = "💪 %d weitere Kalorien benötigt"
        goalAchieved = "✅ Ziel erreicht!"
        caloriesRemaining = "%d Kalorien verbleibend"
        exceededBy = "Überschritten um %d Kalorien"
        perfect = "Perfekt!"
        progressPercent = "Fortschritt: %.1f%%"

        yourNutritionalProfile = "Ihr Ernährungsprofil"
        profileComplete = "Profil vollständig"
        personalInfo = "Persönliche Informationen"
        activityAndGoal = "Aktivität & Ziel"
        ageLabel = "Alter"
        sexLabel = "Geschlecht"
        heightLabel = "Größe"
        weightLabel = "Gewicht"
        activityLabel = "Aktivität"
        goalLabel = "Ziel"
        years = "Jahre"
        cm = "cm"
        kg = "kg"
        recommendedDailyCalories = "Empfohlene tägliche Kalorien: %d kcal"
        disclaimerText =
            "⚠️ Haftungsausschluss: Dies sind nur Empfehlungen. Konsultieren Sie einen Arzt, bevor Sie wesentliche Änderungen an Ihrer Ernährung vornehmen."
        noProfileDataFound =
            "Keine Profildaten gefunden. Klicken Sie unten, um Ihr Alter, Geschlecht, Größe, Gewicht, Ziele und Aktivitätsniveau einzurichten."
        updateProfile = "Profil aktualisieren"
        setupProfile = "Profil einrichten"

        sedentary = "Bewegungsarm (kaum oder keine Bewegung)"
        lightExercise = "Leichte Bewegung (1-3 Tage/Woche)"
        moderateExercise = "Mäßige Bewegung (3-5 Tage/Woche)"
        active = "Aktiv (6-7 Tage/Woche)"
        veryActive = "Sehr aktiv (tägliche intensive Bewegung)"

        extremeLoss = "Extreme Gewichtsabnahme (1000 kcal Defizit)"
        moderateLoss = "Mäßige Gewichtsabnahme (500 kcal Defizit)"
        maintain = "Gewicht halten"
        moderateGain = "Mäßige Gewichtszunahme (500 kcal Überschuss)"
        extremeGain = "Extreme Gewichtszunahme (1000 kcal Überschuss)"

        macros = "Makros"
        macrosFor = "Makros für "
        kcalShort = "Kcal"
        proteinShort = "Protein"
        carbsShort = "Kohlenhydrate"
        fatsShort = "Fett"
        amountLabel = "Menge: %.1fg"
        quantityLabelMeal = "Menge: %.1fg"
        ok = "OK"
        afterAddingDeficit = "Nach dem Hinzufügen: 🎯 %d kcal übrig, um im Defizit zu bleiben"
        afterAddingExceeded = "⚠️ Nach dem Hinzufügen: Sie werden Ihr Kalorienziel überschreiten"
        afterAddingSurplus =
            "Nach dem Hinzufügen: 💪 %d weitere kcal benötigt, um den Überschuss zu erreichen"
        afterAddingGoalMet =
            "✅ Nach dem Hinzufügen: Sie werden Ihr Überschussziel erreichen oder übertreffen"
        remainingToMaintain = "%d kcal verbleibend, um das Gewicht zu halten"
        exceedMaintenance = "Wartung um %d kcal überschritten"
        perfectMaintain = "Perfekt! Sie werden genau Ihr Erhaltungsziel erreichen"
        effectOnGoal = "Auswirkung auf Ihr tägliches Ziel"
        noGoalSet = "Kein Ziel gesetzt"
        recommendSetGoal =
            "Wir empfehlen, ein Hauptziel in den Einstellungen festzulegen, für ein besseres Erlebnis"
        previewMacroIntake = "Vorschau der Makroaufnahme"
        pleaseSelectProduct = "Bitte wählen Sie ein Produkt aus, um fortzufahren"
        addToToday = "Heute hinzufügen"
        selectMeal = "Wählen Sie eine Mahlzeit"
        mealContains = "Mahlzeit enthält:"
        totalMealWeight = "Gesamtgewicht der Mahlzeit: %sg"
        quantityConsumed = "Verzehrte Menge (g)"
        servingFactor = "Portionsfaktor: %sx"
        macroPreview = "Makro-Vorschau"
        pleaseSelectMeal = "Bitte wählen Sie eine Mahlzeit"
        amountGrams = "Menge (g)"

        stepTracker = "Schrittzähler"
        stepsCount = "%d Schritte"
        goalSteps = "Ziel: %d Schritte"
        percentCompleted = "%d%% abgeschlossen"
        insights = "Einblicke"
        lowActivity = "Geringe Aktivität – versuchen Sie einen kurzen Spaziergang."
        goodProgress = "Guter Fortschritt – Sie werden aktiver."
        greatJob = "Gute Arbeit – fast am Ziel!"
        excellentGoalAchieved = "Ausgezeichnet – Ziel erreicht 🎉"
        remainingStepsLabel = "Verbleibend: %d Schritte"
        history = "Verlauf"
        historyPlaceholder =
            "Der tägliche Verlauf erscheint hier, sobald die Persistenz hinzugefügt wurde."

        basicInformation = "Grundinformationen"
        yourName = "Ihr Name"
        ageYears = "Alter (Jahre)"
        sex = "Geschlecht"
        male = "Männlich"
        female = "Weiblich"
        heightCm = "Größe (cm)"
        weightKg = "Gewicht (kg)"
        activityLevelTitle = "Aktivitätsniveau"
        howOftenExercise = "Wie oft trainieren Sie?"
        yourGoal = "Ihr Ziel"
        baseMaintenance = "Basis-Wartung: %d kcal/Tag"
        completePreviousSteps = "Bitte zuerst die vorherigen Schritte abschließen"
        caloriesPerDayWeeklyChange = "%d kcal/Tag · %s"
        goBackFillInfo =
            "Bitte gehen Sie zurück und stellen Sie sicher, dass alle Informationen ausgefüllt sind."
        back = "Zurück"
        next = "Weiter"
        calculate = "Berechnen"

        totalWaterIntake = "Gesamte Wasseraufnahme"
        mlValue = "%d ml"
        editCupSizes = "Bechergrößen bearbeiten"
        recommendedIntake = "Empfohlene Aufnahme"
        percentOfDailyGoal = "%d%% des Tagesziels"
        todaysHistory = "Heutiger Verlauf"
        editCupSizesTitle = "Bechergrößen bearbeiten (ml)"
        cupLabel = "Becher %d"
        setRecommendedIntake = "Empfohlene tägliche Aufnahme festlegen (ml)"
        recommendedMl = "Empfohlene ml"
        notifications = "Benachrichtigungen"

        nutritionPer100g = "Nährwerte pro 100g"

        permissionsRequiredTitle = "Benötigte Berechtigungen"
        permissionsRequiredDescription =
            "Diese App benötigt Berechtigungen zur Aktivitätserkennung und zum Gesundheitsdienst, um Ihre Schritte zu zählen."
        grantPermissions = "Berechtigungen erteilen"

        cameraPermissionNeeded = "Kameraberechtigung ist zum Scannen von Barcodes erforderlich"
        grantPermission = "Berechtigung erteilen"
        cameraPermissionDenied = "Kameraberechtigung verweigert"
        scanningProgress = "Scan läuft... (%d/3)"
        startingCamera = "Kamera wird gestartet..."
        errorPrefix = "Fehler: "
        positionBarcode =
            "Positionieren Sie den Barcode innerhalb des Rahmens\nMehrfachscan für Genauigkeit..."

        meals = "Mahlzeiten"
    }

    // ------------------------------------------------------------------
    // ROMANIAN
    // ------------------------------------------------------------------
    private val romanianStrings: Strings = Strings().apply {
        appName = "Salubris"
        close = "Închide"
        cancel = "Anulează"
        confirm = "Confirmă"
        save = "Salvează"
        update = "Actualizează"
        delete = "Șterge"
        dismiss = "Renunță"
        retry = "Reîncearcă"
        loading = "Se încarcă..."
        noInternet = "Fără conexiune la internet"
        apiError = "Eroare API – încercați din nou"

        addProduct = "Adaugă Produs"
        scanBarcode = "Scanează Cod"
        noProductsYet = "Încă nu există produse"
        productName = "Nume produs"
        barcodeOptional = "Cod de bare (opțional)"
        caloriesPer100g = "Calorii (kcal)"
        proteinPer100g = "Proteine (g)"
        carbsPer100g = "Carbohidrați (g)"
        fatsPer100g = "Grăsimi (g)"
        nutritionalValuesPer100g = "Valorile nutriționale sunt per 100g"
        addProductTitle = "Adaugă un produs"
        editProductTitle = "Editează Produs"
        productAlreadyExists = "Produsul există deja"
        productNotFound = "Produsul nu a fost găsit"
        dataFromOpenFoodFacts =
            "Date preluate de la Open Food Facts. Pot fi învechite sau incomplete."
        visitOpenFoodFacts = "Vizitează Open Food Facts"
        saveProduct = "Salvează Produs"
        updateProduct = "Actualizează Produs"

        addMeal = "Adaugă Masă"
        handsFree = "Mâini libere"
        noMealsYet = "Încă nu există mese"
        addMealTitle = "Adaugă o masă"
        handsFreeMealTitle = "Masă cu mâini libere"
        mealName = "Nume masă"
        products = "Produse"
        selectProduct = "Selectează un produs"
        quantityGrams = "Cantitate (g)"
        addProductButton = "ADAUGA PRODUS"
        saveMeal = "Salvează Masa"
        addedProducts = "Produse adăugate"
        draftProducts = "Produse în așteptare"
        resolve = "Rezolvă"
        removeDraft = "Elimină draft"
        noProductsAdded = "Niciun produs adăugat încă"
        noDrafts = "Niciun draft"
        resolveDraftTitle = "Rezolvă draft: "
        mapExisting = "Asociază cu produs existent"
        createNew = "Creează produs nou"
        newProductName = "Nume produs"
        calories = "Calorii"
        protein = "Proteine"
        carbs = "Carbohidrați"
        fats = "Grăsimi"
        totals = "Total:"
        per100g = "per 100g"
        ingredients = "ingredient"
        ingredientsPlural = "ingrediente"
        deleteMeal = "Șterge masa"
        remove = "Elimină"
        quantityLabel = "Cantitate:"

        ready = "Gata"
        microphonePermissionDenied = "Permisiunea pentru microfon a fost refuzată"
        listening = "Ascult..."
        speaking = "Vorbești..."
        processing = "Procesez..."
        notListening = "Nu ascult"
        commandNotRecognized = "Comandă nerecunoscută (începe cu 'add')"
        noProductName = "Niciun nume de produs"
        pleaseSayQuantity = "Te rog spune cantitatea pentru {product} (ex: '100 de grame')"
        addedProduct = "Adăugat {quantity}g de {product}"
        productNotFoundAddedDraft = "{product} negăsit – adăugat ca draft"
        resolvedProduct = "Rezolvat: {product}"
        couldNotUnderstand = "Nu am putut înțelege"
        noCommandRecognized = "Nicio comandă recunoscută"
        recognizerBusy = "Recunoscător ocupat"
        errorWithCode = "Eroare: {error}"
        resolved = "Rezolvat"
        drafts = "Draft-uri"
        pleaseEnterMealName = "Te rog introdu un nume pentru masă"
        addAtLeastOneProduct = "Adaugă cel puțin un produs sau draft"
        warning = "Atenție"
        link = "Link"
        speechPrompt = "Spune 'add produs cantitate'"

        aiAssistant = "Asistent AI"
        aiAssistantLoading = "Se încarcă asistentul..."
        failedToLoadModel = "Eroare la încărcarea modelului"
        unknownError = "Eroare necunoscută"
        copy = "Copiază"
        source = "Sursă"
        askMeAnything = "Întreabă-mă orice..."
        send = "Trimite"
        chatGreeting =
            "👋 Bună! Sunt asistentul tău Salubris. Caut surse de nutriție de încredere pentru ca tu să nu trebuiască să o faci. Întreabă-mă despre macro, calorii sau totaluri de mese!\n\nScrie 'Ajutor' pentru un ghid și exemple."
        chatHelpGuide =
            "🤖 **Asistent Nutrițional Salubris**\n\nCaut surse de încredere (USDA, Open Food Facts și web) pentru a-ți oferi date precise de macronutrienți per 100 de grame. Pot calcula și totalurile meselor atunci când oferi cantități.\n\nIată ce mă poți întreba:\n\n• **Macro pentru un singur aliment**\n   - \"macro piept de pui per 100g\"\n   - \"calorii avocado 100 de grame\"\n   - \"proteine somon per 100g\"\n\n• **Calculul unei mese** (adaugă cantități)\n   - \"200g piept de pui, 150g orez, 100g broccoli\"\n   - \"300g friptură, 200g cartofi, 50g unt\"\n\n• **Întrebări generale despre nutriție și sănătate**\n   - \"De câte proteine am nevoie pe zi?\"\n   - \"Care sunt sursele sănătoase de grăsimi?\"\n   - \"Este eficient postul intermitent?\"\n\nScrie întrebarea ta normal – voi detecta ce ai nevoie și voi căuta pe web date verificate. Nu inventez niciodată numere."
        chatPromptForQuantities =
            "Te rog furnizează cantități pentru fiecare aliment pentru a putea calcula macro-urile mesei.\n\nExemplu:\n200g piept de pui\n150g orez"
        chatInvalidQuantityFormat =
            "Nu am putut înțelege cantitățile. Te rog folosește formatul: 200g piept de pui."
        chatNoDataForIngredients = "Nu s-au găsit date nutriționale pentru acele ingrediente."
        chatNutritionAssistantFallback =
            "🤔 Sunt un asistent nutrițional. Pot ajuta cu macro-uri pentru alimente, calcule de mese și întrebări generale despre nutriție.\n\nScrie 'Ajutor' pentru a vedea ce pot face."
        chatErrorTemplate = "❌ Ceva nu a funcționat: %s"
        noReliableData = "🤔 Nu am găsit date nutriționale de încredere pentru acel aliment."
        addToMyProducts = "Adaugă la Produsele Mele"
        valuesPer100g = "⚠️ Valorile sunt per 100 de grame."
        openSource = "Deschide sursa"
        addToProducts = "Adaugă la produse"
        copyMessage = "Copiază mesajul"
        failedToGenerateResponse = "🤔 Nu am putut genera un răspuns."
        defaultFallbackResponse =
            "Îmi pare rău, nu am putut procesa această cerere. Încearcă să întrebi despre un aliment anume sau scrie 'Ajutor' pentru exemple."

        barcodeScanner = "Scanner cod de bare"
        lookingUpProduct = "Se caută produsul..."
        noInternetConnection = "Fără conexiune la internet"
        productAlreadyExistsSnack = "Produsul există deja"
        productNotFoundSnack = "Produsul nu a fost găsit în Open Food Facts"
        apiErrorSnack = "Eroare API – încercați din nou"

        home = "Acasă"
        tracking = "Urmărire"
        productsNav = "Produse"
        mealsNav = "Mese"
        settingsNav = "Setări"
        menu = "Meniu"
        customizeFooter = "Personalizează Footer"
        pages = "Pagini"
        maximumPagesSelected = "Maximum 4 pagini selectate"
        quickActions = "Acțiuni rapide"
        selected = "Selectat"
        notSelected = "Neselectat"

        settings = "Setări"
        profileSetup = "Configurare profil"
        language = "Limbă"
        selectLanguage = "Selectați limba"
        english = "Engleză"
        spanish = "Spaniolă"
        french = "Franceză"
        german = "Germană"
        romanian = "Română"

        welcomeBack = "Bun venit înapoi"
        userDefault = "Utilizator"
        userIcon = "Pictogramă utilizator"
        steps = "Pași"
        stepsIcon = "Pași"
        remainingSteps = "pași rămași"
        stepSensorNotAvailable = "Senzorul de pași nu este disponibil"
        water = "Apă"
        waterIcon = "Apă"
        mlRemaining = "ml rămași"
        todaysIntake = "Aportul de astăzi"
        caloriesIcon = "Calorii"
        noDataThisWeek = "Nicio dată în această săptămână"
        analytics = "Analiză"
        analyticsIcon = "Analiză"
        weeklyCaloricIntake = "Aport caloric săptămânal"
        refreshIcon = "Reîmprospătează"
        caloriesLeft = "🎯 %d calorii rămase"
        exceededGoal = "⚠️ Ți-ai depășit obiectivul"
        moreCaloriesNeeded = "💪 %d calorii suplimentare necesare"
        goalAchieved = "✅ Obiectiv atins!"
        caloriesRemaining = "%d calorii rămase"
        exceededBy = "Depășit cu %d calorii"
        perfect = "Perfect!"
        progressPercent = "Progres: %.1f%%"

        yourNutritionalProfile = "Profilul tău nutrițional"
        profileComplete = "Profil complet"
        personalInfo = "Informații personale"
        activityAndGoal = "Activitate și Obiectiv"
        ageLabel = "Vârstă"
        sexLabel = "Sex"
        heightLabel = "Înălțime"
        weightLabel = "Greutate"
        activityLabel = "Activitate"
        goalLabel = "Obiectiv"
        years = "ani"
        cm = "cm"
        kg = "kg"
        recommendedDailyCalories = "Calorii zilnice recomandate: %d kcal"
        disclaimerText =
            "⚠️ Avertisment: Acestea sunt doar recomandări. Consultați un profesionist în sănătate înainte de a face modificări semnificative în dietă."
        noProfileDataFound =
            "Nu s-au găsit date de profil. Faceți clic mai jos pentru a vă configura vârsta, sexul, înălțimea, greutatea, obiectivele și nivelul de activitate."
        updateProfile = "Actualizează profilul"
        setupProfile = "Configurează profilul"

        sedentary = "Sedentar (puțin sau deloc exercițiu)"
        lightExercise = "Exercițiu ușor (1-3 zile/săptămână)"
        moderateExercise = "Exercițiu moderat (3-5 zile/săptămână)"
        active = "Activ (6-7 zile/săptămână)"
        veryActive = "Foarte activ (exercițiu intens zilnic)"

        extremeLoss = "Pierdere extremă în greutate (deficit de 1000 kcal)"
        moderateLoss = "Pierdere moderată în greutate (deficit de 500 kcal)"
        maintain = "Menține greutatea"
        moderateGain = "Creștere moderată în greutate (surplus de 500 kcal)"
        extremeGain = "Creștere extremă în greutate (surplus de 1000 kcal)"

        macros = "Macros"
        macrosFor = "Macros pentru "
        kcalShort = "Kcal"
        proteinShort = "Proteine"
        carbsShort = "Carbohidrați"
        fatsShort = "Grăsimi"
        amountLabel = "Cantitate: %.1fg"
        quantityLabelMeal = "Cantitate: %.1fg"
        ok = "OK"
        afterAddingDeficit = "După adăugare: 🎯 %d kcal rămase pentru a menține deficitul"
        afterAddingExceeded = "⚠️ După adăugare: Vei depăși obiectivul caloric"
        afterAddingSurplus =
            "După adăugare: 💪 %d kcal suplimentare necesare pentru a atinge surplusul"
        afterAddingGoalMet = "✅ După adăugare: Vei atinge sau depăși obiectivul de surplus"
        remainingToMaintain = "%d kcal rămase pentru a menține greutatea"
        exceedMaintenance = "Vei depăși mentenanța cu %d kcal"
        perfectMaintain = "Perfect! Vei atinge exact obiectivul de menținere"
        effectOnGoal = "Efect asupra obiectivului zilnic"
        noGoalSet = "Niciun obiectiv setat"
        recommendSetGoal =
            "Recomandăm să setați un obiectiv principal în secțiunea de setări pentru o experiență mai bună"
        previewMacroIntake = "Previzualizare aport macro"
        pleaseSelectProduct = "Selectați un produs pentru a continua"
        addToToday = "Adaugă la azi"
        selectMeal = "Selectează o masă"
        mealContains = "Masa conține:"
        totalMealWeight = "Greutatea totală a mesei: %sg"
        quantityConsumed = "Cantitate consumată (g)"
        servingFactor = "Factor porție: %sx"
        macroPreview = "Previzualizare macro"
        pleaseSelectMeal = "Selectează o masă"
        amountGrams = "Cantitate (g)"

        stepTracker = "Urmărire Pași"
        stepsCount = "%d pași"
        goalSteps = "Obiectiv: %d pași"
        percentCompleted = "%d%% completat"
        insights = "Informații"
        lowActivity = "Activitate scăzută – încearcă să faci o plimbare scurtă."
        goodProgress = "Progres bun – te miști."
        greatJob = "Bravo – aproape ai atins obiectivul!"
        excellentGoalAchieved = "Excelent – obiectiv atins 🎉"
        remainingStepsLabel = "Rămași: %d pași"
        history = "Istoric"
        historyPlaceholder =
            "Istoricul zilnic de urmărire va apărea aici odată ce se adaugă persistența."

        basicInformation = "Informații de bază"
        yourName = "Numele tău"
        ageYears = "Vârsta (ani)"
        sex = "Sex"
        male = "Masculin"
        female = "Feminin"
        heightCm = "Înălțime (cm)"
        weightKg = "Greutate (kg)"
        activityLevelTitle = "Nivel de activitate"
        howOftenExercise = "Cât de des faci exerciții?"
        yourGoal = "Obiectivul tău"
        baseMaintenance = "Mentenanță de bază: %d kcal/zi"
        completePreviousSteps = "Completează pașii anteriori mai întâi"
        caloriesPerDayWeeklyChange = "%d kcal/zi · %s"
        goBackFillInfo =
            "Te rog să te întorci și să te asiguri că toate informațiile sunt completate."
        back = "Înapoi"
        next = "Următorul"
        calculate = "Calculează"

        totalWaterIntake = "Aport total de apă"
        mlValue = "%d ml"
        editCupSizes = "Editează mărimile paharelor"
        recommendedIntake = "Aport recomandat"
        percentOfDailyGoal = "%d%% din obiectivul zilnic"
        todaysHistory = "Istoricul de azi"
        editCupSizesTitle = "Editează Mărimile Paharelor (ml)"
        cupLabel = "Pahar %d"
        setRecommendedIntake = "Setează Aportul Zilnic Recomandat (ml)"
        recommendedMl = "ml recomandați"
        notifications = "Notificări"

        nutritionPer100g = "Nutriție per 100g"

        permissionsRequiredTitle = "Permisiuni necesare"
        permissionsRequiredDescription =
            "Această aplicație are nevoie de permisiunile de recunoaștere a activității și serviciu de sănătate pentru a număra pașii."
        grantPermissions = "Acordă permisiuni"

        cameraPermissionNeeded = "Permisiunea camerei este necesară pentru a scana codurile de bare"
        grantPermission = "Acordă permisiune"
        cameraPermissionDenied = "Permisiunea camerei refuzată"
        scanningProgress = "Se scanează... (%d/3)"
        startingCamera = "Se pornește camera..."
        errorPrefix = "Eroare: "
        positionBarcode =
            "Poziționează codul de bare în interiorul cadrului\nScanare de mai multe ori pentru precizie..."

        meals = "Mese"
    }

    // Helper to copy one Strings instance to another (unused here since we manually assign everything)
    private fun Strings.copyFrom(other: Strings) {
        // Not needed because we assign all fields directly in each language block.
    }

    // Not-null delegate
    private fun <T> notNull() = object : kotlin.properties.ReadWriteProperty<Any?, T> {
        private var value: T? = null
        override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T {
            return value ?: throw IllegalStateException("Property ${property.name} not initialized")
        }

        override fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: T) {
            this.value = value
        }
    }
}