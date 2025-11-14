package com.example.medijourney.modules.profile.membership_plan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.SelectionItemModel
import com.example.medijourney.common.models.realm_models.Membership
import com.example.medijourney.common.models.realm_models.UserMembership
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.MembershipRepo
import com.example.medijourney.common.respositories.UserMembershipRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembershipPlanViewModel @Inject constructor(
    membershipRepo: MembershipRepo,
    private val userMembershipRepo: UserMembershipRepo
) : ViewModel() {

    // Properties
    val membershipPlans = MutableLiveData<MutableList<SelectionItemModel>>()
    private var membershipsFlow = membershipRepo.getMembershipsFlow()
    private var userMembershipFlow = userMembershipRepo.getUserMembershipFow()
    private var userMembershipId = ""

    // Life cycle
    init {
        viewModelScope.launch {
            observeData()
        }
    }

    // Functions
    private suspend fun observeData() {

        membershipsFlow
            .combine(userMembershipFlow) { membershipResult, userMembershipResult ->
                userMembershipId = userMembershipResult?.id ?: ""
                Pair(membershipResult, userMembershipResult)
            }
            .firstThenDebounce(500L)
            .collect {
                val dataList = generateCellModel(it.first, it.second)
                membershipPlans.postValue(dataList)
            }
    }

    private fun generateCellModel(
        membershipList: List<Membership>,
        userMembership: UserMembership?
    ): MutableList<SelectionItemModel> {
        val userMembership = userMembership ?: return  mutableListOf()
        if (!userMembership.isValid()) return mutableListOf()

        val dataList: MutableList<SelectionItemModel> = mutableListOf()

        membershipList.forEach { membership ->
            if (membership.isValid() && userMembership.isValid()) {
                val title = MTextStyle(membership.name ?: "")
                val selectionItemModel = SelectionItemModel(
                    data = membership,
                    title = title,
                    isSelected = userMembership.id == membership.id,
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
        val model = model as? SelectionItemModel ?: return completion(false)
        if (model.isSelected) return completion(false)

        viewModelScope.launch {
            val result = userMembershipRepo.updateMembership(userMembershipId, mapOf("membership_id" to membership.id))
            completion(result)
        }
    }
}