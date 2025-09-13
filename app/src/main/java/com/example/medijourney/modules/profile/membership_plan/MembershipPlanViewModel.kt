package com.example.medijourney.modules.profile.membership_plan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.managers.realm.where
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.SelectionItemModel
import com.example.medijourney.common.models.realm_models.Membership
import com.example.medijourney.common.models.realm_models.UserMedicalSpecialty
import com.example.medijourney.common.models.realm_models.UserMembership
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.MTextStyle
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("NAME_SHADOWING")
class MembershipPlanViewModel : ViewModel() {

    // Properties
    val membershipPlans = MutableLiveData<MutableList<SelectionItemModel>>()
    private var membershipResult: RealmResults<Membership>? = null
    private var userMembershipResult: RealmResults<UserMembership>? = null

    // Life cycle
    init {
        viewModelScope.launch {
            getData()
            observeData()
        }
        membershipPlans.postValue(generateCellModel())
    }

    // Functions
    private suspend fun getData() {
        membershipResult = RealmManager.read(Membership::class.java,
            sort = listOf(Pair(Membership::position.name, Sort.ASCENDING)))
        FirebaseAuthManager.getCurrentUserCode()?.let {
            userMembershipResult = RealmManager.read(UserMembership::class.java,
                realmQuery = where(UserMedicalSpecialty::userId.name, Operator.EQUAL, it)
            )
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private suspend fun observeData() {
        val membershipResult = membershipResult ?: return
        val userMembershipResult = userMembershipResult ?: return

        combine(
            membershipResult.asFlow(),
            userMembershipResult.asFlow(),
        ) { membershipResult, userMembershipResult ->
            this.membershipResult = membershipResult.list
            this.userMembershipResult = userMembershipResult.list
        }
            .debounce(500)
            .collectLatest {
                val modelList = generateCellModel()
                withContext(Dispatchers.Main) {
                    membershipPlans.postValue(modelList)
                }
            }
    }

    private fun generateCellModel(): MutableList<SelectionItemModel> {
        val membershipResult = membershipResult ?: return  mutableListOf()
        val userMembershipResult = userMembershipResult ?: return  mutableListOf()
        val userMembership = userMembershipResult.firstOrNull() ?: return  mutableListOf()

        val dataList: MutableList<SelectionItemModel> = mutableListOf()

        membershipResult.forEach { membership ->
            if (membership.isValid() && userMembership.isValid()) {
                val title = MTextStyle(membership.name ?: "")
                val selectionItemModel = SelectionItemModel(
                    data = membership,
                    title = title,
                    isSelected = userMembershipResult.any { userMembership ->
                        userMembership.membershipId == membership.id
                    },
                    padding = EdgePadding(16, 16, 16, 16),
                    isClickable = false
                )
                dataList.add(selectionItemModel)
            }
        }

        return dataList
    }

    fun handleSelectedItem(model: BaseItemInterface?, completion: (Boolean) -> Unit) {
        val membership = model?.data as? Membership ?: return completion(false)
        val userMembershipResult = userMembershipResult ?: return completion(false)
        val userMembership = userMembershipResult.firstOrNull() ?: return completion(false)

        if (userMembership.isValid() && userMembership.membershipId != membership.id) {
            FireStoreManager.buildDoc(
                Pair(FireStoreCollection.USER_MEMBERS, userMembership.userId),
                Pair(FireStoreCollection.USER_MEMBERSHIP, userMembership.id))
                .update(mapOf("membership_id" to membership.id))
                .addOnCompleteListener {
                    completion(it.isSuccessful)
                }
        }
    }
}