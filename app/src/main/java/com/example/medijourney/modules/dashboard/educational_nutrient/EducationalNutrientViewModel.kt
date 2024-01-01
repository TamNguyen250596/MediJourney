package com.example.medijourney.modules.dashboard.educational_nutrient

import android.net.Uri
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.api.NutritionixApi
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.google.gson.JsonElement
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EducationalNutrientViewModel : ViewModel() {

    // Properties
    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _showPlaceholder = MutableStateFlow(value = true)
    val showPlaceholder: StateFlow<Boolean> = _showPlaceholder.asStateFlow()
    private val _photoUrl = MutableStateFlow<Uri?>(null)
    val photoUrl: StateFlow<Uri?> = _photoUrl.asStateFlow()
    private val _name = MutableStateFlow<String?>(null)
    val name: StateFlow<String?> = _name.asStateFlow()
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    val searchTextFlow = MutableStateFlow<String?>(null)

    // Life cycle
    fun onViewCreated() {
        viewModelScope.launch {
            observeSearchText()
        }
    }

    // Functions
    @OptIn(FlowPreview::class)
    private suspend fun observeSearchText() {
        searchTextFlow
            .debounce(500)
            .collect {
                requestAPIToGetNutritionInfo(it)
            }
    }

    private fun requestAPIToGetNutritionInfo(searchText: String?) {
        val searchQuery = searchText ?: return

        _isLoading.value = true
        val retrofit = Retrofit.Builder()
            .baseUrl("https://trackapi.nutritionix.com/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(NutritionixApi::class.java)
        val body = mapOf("query" to searchQuery)

        api.getFoodInfo(body).enqueue(object : Callback<JsonElement> {
            override fun onResponse(
                call: Call<JsonElement?>,
                response: Response<JsonElement?>
            ) {
                handleResponse(response)
                _isLoading.value = false
                _showPlaceholder.value = !response.isSuccessful
            }

            override fun onFailure(
                call: Call<JsonElement?>,
                t: Throwable
            ) {
                _isLoading.value = false
                _showPlaceholder.value = true
            }
        })
    }

    private fun handleResponse(response: Response<JsonElement?>) {
        val body = response.body() ?: return
        val json = body.asJsonObject ?: return
        val foods = json.get("foods")?.asJsonArray ?: return
        val food = foods.firstOrNull()?.asJsonObject?: return

        val photo = food["photo"].asJsonObject
        if (photo != null) {
            val photoUrl = photo["highres"].asString
            photoUrl?.let {
                _photoUrl.value = photoUrl.toUri()
            }
        }

        val name = food["food_name"].asString
        if (name != null) {
            _name.value = name.capitalize(Locale.current)
        }

        val fullNutrients = food["full_nutrients"].asJsonArray
        if (!fullNutrients.isEmpty) {
            _itemModels.value = fullNutrients.mapNotNull {
                val item = it.asJsonObject ?: return@mapNotNull null
                val nutrientId = item["attr_id"].asInt
                val info = getNutrientInfo(nutrientId) ?: return@mapNotNull null
                val value = item["value"].asDouble

                DynamicUIItem(
                    type = Constants.ITEM,
                    itemTag = nutrientId.toString(),
                    groupIndex = 0,
                    data = it,
                    backgroundColor = 0,
                ).apply {
                    info.first.let {
                        title = MTextStyle(it)
                    }
                    info.second.let {
                        description = MTextStyle("$value $it")
                    }
                }
            }
        }
    }

    private fun getNutrientInfo(id: Int): Pair<String, String>? {
        return when(id) {
            301 -> Pair("Calcium, Ca", "mg")
            205 -> Pair("Carbohydrate, by difference", "g")
            601 -> Pair("Cholesterol", "mg")
            208 -> Pair("Energy", "kcal")
            606 -> Pair("Fatty acids, total saturated", "g")
            204 -> Pair("Total lipid (fat)", "g")
            605 -> Pair("Fatty acids, total trans", "g")
            303 -> Pair("Iron, Fe", "mg")
            291 -> Pair("Fiber, total dietary", "g")
            306 -> Pair("Potassium, K", "mg")
            307 -> Pair("Sodium, Na", "mg")
            203 -> Pair("Protein", "g")
            269 -> Pair("Sugars, total", "g")
            539 -> Pair("Sugars, added", "g")
            324 -> Pair("Vitamin D", "IU")
            513 -> Pair("Alanine", "g")
            221 -> Pair("Alcohol, ethyl", "g")
            511 -> Pair("Arginine", "g")
            207 -> Pair("Ash", "g")
            514 -> Pair("Aspartic acid", "g")
            454 -> Pair("Betaine", "mg")
            262 -> Pair("Caffeine", "mg")
            639 -> Pair("Campesterol", "mg")
            322 -> Pair("Carotene, alpha", "µg")
            321 -> Pair("Carotene, beta", "µg")
            326 -> Pair("Vitamin D3 (cholecalciferol)", "µg")
            421 -> Pair("Choline, total", "mg")
            334 -> Pair("Cryptoxanthin, beta", "µg")
            312 -> Pair("Copper, Cu", "mg")
            507 -> Pair("Cystine", "g")
            268 -> Pair("Energy", "kJ")
            325 -> Pair("Vitamin D2 (ergocalciferol)", "µg")
            610 -> Pair("10:00", "g")
            611 -> Pair("12:00", "g")
            696 -> Pair("13:00", "g")
            612 -> Pair("14:00", "g")
            625 -> Pair("14:01", "g")
            652 -> Pair("15:00", "g")
            697 -> Pair("15:01", "g")
            613 -> Pair("16:00", "g")
            626 -> Pair("16:1 undifferentiated", "g")
            673 -> Pair("16:1 c", "g")
            662 -> Pair("16:1 t", "g")
            653 -> Pair("17:00", "g")
            687 -> Pair("17:01", "g")
            614 -> Pair("18:00", "g")
            617 -> Pair("18:1 undifferentiated", "g")
            674 -> Pair("18:1 c", "g")
            663 -> Pair("18:1 t", "g")
            859 -> Pair("18:1-11t (18:1t n-7)", "g")
            618 -> Pair("18:2 undifferentiated", "g")
            670 -> Pair("18:2 CLAs", "g")
            675 -> Pair("18:2 n-6 c,c", "g")
            669 -> Pair("18:2 t,t", "g")
            619 -> Pair("18:3 undifferentiated", "g")
            851 -> Pair("18:3 n-3 c,c,c (ALA)", "g")
            685 -> Pair("18:3 n-6 c,c,c", "g")
            627 -> Pair("18:04", "g")
            615 -> Pair("20:00", "g")
            628 -> Pair("20:01", "g")
            672 -> Pair("20:2 n-6 c,c", "g")
            689 -> Pair("20:3 undifferentiated", "g")
            852 -> Pair("20:3 n-3", "g")
            853 -> Pair("20:3 n-6", "g")
            620 -> Pair("20:4 undifferentiated", "g")
            855 -> Pair("20:4 n-6", "g")
            629 -> Pair("20:5 n-3 (EPA)", "g")
            857 -> Pair("21:05", "g")
            624 -> Pair("22:00", "g")
            630 -> Pair("22:1 undifferentiated", "g")
            858 -> Pair("22:04", "g")
            631 -> Pair("22:5 n-3 (DPA)", "g")
            621 -> Pair("22:6 n-3 (DHA)", "g")
            654 -> Pair("24:00:00", "g")
            671 -> Pair("24:1 c", "g")
            607 -> Pair("04:00", "g")
            608 -> Pair("06:00", "g")
            609 -> Pair("08:00", "g")
            645 -> Pair("Fatty acids, total monounsaturated", "g")
            646 -> Pair("Fatty acids, total polyunsaturated", "g")
            693 -> Pair("Fatty acids, total trans-monoenoic", "g")
            695 -> Pair("Fatty acids, total trans-polyenoic", "g")
            313 -> Pair("Fluoride, F", "µg")
            417 -> Pair("Folate, total", "µg")
            431 -> Pair("Folic acid", "µg")
            435 -> Pair("Folate, DFE", "µg")
            432 -> Pair("Folate, food", "µg")
            212 -> Pair("Fructose", "g")
            287 -> Pair("Galactose", "g")
            515 -> Pair("Glutamic acid", "g")
            211 -> Pair("Glucose (dextrose)", "g")
            516 -> Pair("Glycine", "g")
            512 -> Pair("Histidine", "g")
            521 -> Pair("Hydroxyproline", "g")
            503 -> Pair("Isoleucine", "g")
            213 -> Pair("Lactose", "g")
            504 -> Pair("Leucine", "g")
            338 -> Pair("Lutein + zeaxanthin", "µg")
            337 -> Pair("Lycopene", "µg")
            505 -> Pair("Lysine", "g")
            214 -> Pair("Maltose", "g")
            506 -> Pair("Methionine", "g")
            304 -> Pair("Magnesium, Mg", "mg")
            428 -> Pair("Menaquinone-4", "µg")
            315 -> Pair("Manganese, Mn", "mg")
            406 -> Pair("Niacin", "mg")
            573 -> Pair("Vitamin E, added", "mg")
            578 -> Pair("Vitamin B-12, added", "µg")
            257 -> Pair("Adjusted Protein", "g")
            664 -> Pair("22:1 t", "g")
            676 -> Pair("22:1 c", "g")
            856 -> Pair("18:3i", "g")
            665 -> Pair("18:2 t not further defined", "g")
            666 -> Pair("18:2 i", "g")
            305 -> Pair("Phosphorus, P", "mg")
            410 -> Pair("Pantothenic acid", "mg")
            508 -> Pair("Phenylalanine", "g")
            636 -> Pair("Phytosterols", "mg")
            517 -> Pair("Proline", "g")
            319 -> Pair("Retinol", "µg")
            405 -> Pair("Riboflavin", "mg")
            317 -> Pair("Selenium, Se", "µg")
            518 -> Pair("Serine", "g")
            641 -> Pair("Beta-sitosterol", "mg")
            209 -> Pair("Starch", "g")
            638 -> Pair("Stigmasterol", "mg")
            210 -> Pair("Sucrose", "g")
            263 -> Pair("Theobromine", "mg")
            404 -> Pair("Thiamin", "mg")
            502 -> Pair("Threonine", "g")
            323 -> Pair("Vitamin E (alpha-tocopherol)", "mg")
            341 -> Pair("Tocopherol, beta", "mg")
            343 -> Pair("Tocopherol, delta", "mg")
            342 -> Pair("Tocopherol, gamma", "mg")
            501 -> Pair("Tryptophan", "g")
            509 -> Pair("Tyrosine", "g")
            510 -> Pair("Valine", "g")
            318 -> Pair("Vitamin A, IU", "IU")
            320 -> Pair("Vitamin A, RAE", "µg")
            418 -> Pair("Vitamin B-12", "µg")
            415 -> Pair("Vitamin B-6", "mg")
            401 -> Pair("Vitamin C, total ascorbic acid", "mg")
            328 -> Pair("Vitamin D (D2 + D3)", "µg")
            430 -> Pair("Vitamin K (phylloquinone)", "µg")
            429 -> Pair("Dihydrophylloquinone", "µg")
            255 -> Pair("Water", "g")
            309 -> Pair("Zinc, Zn", "mg")
            344 -> Pair("Tocotrienol, alpha", "mg")
            345 -> Pair("Tocotrienol, beta", "mg")
            346 -> Pair("Tocotrienol, gamma", "mg")
            347 -> Pair("Tocotrienol,delta", "mg")
            else -> null
        }
    }
}
