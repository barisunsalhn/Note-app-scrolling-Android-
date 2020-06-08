package com.noto.domain.interactor.noto

import com.noto.domain.model.Label
import com.noto.domain.model.Noto
import com.noto.domain.repository.NotoRepository

class CreateNoto(private val notoRepository: NotoRepository) {

    suspend operator fun invoke(noto: Noto, labels: List<Label>) = notoRepository.createNoto(noto, labels)

}