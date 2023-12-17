/*
 * Copyright (C)  Tony Green, LitePal Framework Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package litepal.crud;

import java.lang.reflect.InvocationTargetException;

import litepal.LitePalBase;
import litepal.crud.LitePalSupport;
import litepal.crud.model.AssociationsInfo;
import litepal.util.DBUtility;

/**
 * Deals analysis work when comes to two models are associated with One2One
 * associations.
 *
 * @author Tony Green
 * @since 1.1
 */
public class One2OneAnalyzer extends litepal.crud.AssociationsAnalyzer {

    /**
     * Analyzing the AssociationInfo. It will help baseObj assign the necessary
     * values automatically. If the two associated models have bidirectional
     * associations in class files but developer has only build unidirectional
     * associations in models, it will force to build the bidirectional
     * associations. Besides
     * {@link litepal.crud.LitePalSupport#addAssociatedModelWithFK(String, long)} and
     * {@link litepal.crud.LitePalSupport#addAssociatedModelWithoutFK(String, long)} will be
     * called here to put right values into tables.
     *
     * @param baseObj         The baseObj currently want to persist or update.
     * @param associationInfo The associated info analyzed by
     *                        {@link LitePalBase#getAssociationInfo(String)}.
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    void analyze(litepal.crud.LitePalSupport baseObj, AssociationsInfo associationInfo) throws SecurityException,
            IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        litepal.crud.LitePalSupport associatedModel = getAssociatedModel(baseObj, associationInfo);
        if (associatedModel != null) {
            buildBidirectionalAssociations(baseObj, associatedModel, associationInfo);
            dealAssociatedModel(baseObj, associatedModel, associationInfo);
        } else {
            String tableName = DBUtility.getTableNameByClassName(associationInfo
                    .getAssociatedClassName());
            baseObj.addAssociatedTableNameToClearFK(tableName);
        }
    }

    /**
     * Check the association type. If it's bidirectional association, calls
     * {@link #bidirectionalCondition(litepal.crud.LitePalSupport, litepal.crud.LitePalSupport)}. If it's
     * unidirectional association, calls
     * {@link #unidirectionalCondition(litepal.crud.LitePalSupport, litepal.crud.LitePalSupport)}.
     *
     * @param baseObj         The baseObj currently want to persist.
     * @param associatedModel The associated model of baseObj.
     * @param associationInfo The associated info analyzed by
     *                        {@link LitePalBase#getAssociationInfo(String)}.
     */
    private void dealAssociatedModel(litepal.crud.LitePalSupport baseObj, litepal.crud.LitePalSupport associatedModel,
                                     AssociationsInfo associationInfo) {
        if (associationInfo.getAssociateSelfFromOtherModel() != null) {
            bidirectionalCondition(baseObj, associatedModel);
        } else {
            unidirectionalCondition(baseObj, associatedModel);
        }
    }

    /**
     * Deals bidirectional association condition. If associated model is saved,
     * add its' name and id to baseObj by calling
     * {@link litepal.crud.LitePalSupport#addAssociatedModelWithFK(String, long)}. Add its' name
     * and id to baseObj by calling
     * {@link litepal.crud.LitePalSupport#addAssociatedModelWithoutFK(String, long)}.
     *
     * @param baseObj         The baseObj currently want to persist.
     * @param associatedModel The associated model of baseObj.
     */
    private void bidirectionalCondition(litepal.crud.LitePalSupport baseObj, litepal.crud.LitePalSupport associatedModel) {
        if (associatedModel.isSaved()) {
            // use to update associated table after saving
            baseObj.addAssociatedModelWithFK(associatedModel.getTableName(),
                    associatedModel.getBaseObjId());
            // use to add foreign key value while saving
            baseObj.addAssociatedModelWithoutFK(associatedModel.getTableName(),
                    associatedModel.getBaseObjId());
        }
    }

    /**
     * Deals unidirectional associations condition.
     *
     * @param baseObj         The baseObj currently want to persist.
     * @param associatedModel The associated model of baseObj.
     */
    private void unidirectionalCondition(litepal.crud.LitePalSupport baseObj, LitePalSupport associatedModel) {
        dealsAssociationsOnTheSideWithoutFK(baseObj, associatedModel);
    }

}
