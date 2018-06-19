package workspace;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.List;

public class UsePict {
    interface PictLib extends Library {
        // loadLibraryの第一引数はあとで作成するlib***.soの***と一致させる。
        PictLib INSTANCE = (PictLib) Native.loadLibrary("pict", PictLib.class);

        // Cの関数名と一致させる
        Pointer PictCreateTask();
        Pointer PictCreateModel();
        void PictSetRootModel(Pointer task, Pointer model);
        Pointer PictAddParameter(Pointer model, int valueCount, int order, int valueWeights[]);
        Pointer PictAddParameter(Pointer model, int valueCount, int order);
        int PictAddExclusion(Pointer task, PICT_EXCLUSION_ITEM.ByReference exclusionItems, int exclusionItemCount);
        int PictAddSeed(Pointer task, PICT_EXCLUSION_ITEM.ByReference seedItems, int seedItemCount);
        int PictGenerate(Pointer task);
        IntByReference PictAllocateResultBuffer(Pointer task);
        int PictGetTotalParameterCount(Pointer task);
        int PictGetNextResultRow(Pointer task, IntByReference resultRow);
        void PictResetResultFetching( Pointer task );

    }

    public static class PICT_EXCLUSION_ITEM extends Structure {
        public Pointer Parameter;
        public int ValueIndex;
        public static class ByReference extends PICT_EXCLUSION_ITEM implements Structure.ByReference {}

        protected List<String> getFieldOrder() {
            return Arrays.asList("Parameter", "ValueIndex");
        }
    }

    public static void main(String[] args){
        PictLib p = PictLib.INSTANCE;
        Pointer task = p.PictCreateTask();
        p.PictCreateTask();
        Pointer model = p.PictCreateModel();
        p.PictSetRootModel( task, model );

        int weights[] = {1,2,1,1};
        int PAIRWISE = 2;
        Pointer p1 = p.PictAddParameter(model, 4, PAIRWISE, weights);
        Pointer p2 = p.PictAddParameter(model, 3, PAIRWISE);
        Pointer p3 = p.PictAddParameter(model, 5, PAIRWISE);
        Pointer p4 = p.PictAddParameter(model, 2, PAIRWISE);
        Pointer p5 = p.PictAddParameter(model, 4, PAIRWISE);

        // 除外する組み合わせ(p1の0とp2の0)
        int EXCLUSION_1_SIZE = 2;
        PICT_EXCLUSION_ITEM.ByReference excl1_ref = new PICT_EXCLUSION_ITEM.ByReference();
        PICT_EXCLUSION_ITEM[] excl1 = (PICT_EXCLUSION_ITEM[])excl1_ref.toArray(EXCLUSION_1_SIZE);
        excl1[0].Parameter = p1;
        excl1[0].ValueIndex = 0;
        excl1[1].Parameter = p2;
        excl1[1].ValueIndex = 0;
        int ret = p.PictAddExclusion(task, excl1_ref, EXCLUSION_1_SIZE);

        // 除外する組み合わせ(p4の1とp5の2)
        int EXCLUSION_2_SIZE = 2;
        PICT_EXCLUSION_ITEM.ByReference excl2_ref = new PICT_EXCLUSION_ITEM.ByReference();
        PICT_EXCLUSION_ITEM[] excl2 = (PICT_EXCLUSION_ITEM[])excl2_ref.toArray(EXCLUSION_2_SIZE);
        excl2[0].Parameter = p4;
        excl2[0].ValueIndex = 1;
        excl2[1].Parameter = p5;
        excl2[1].ValueIndex = 2;
        ret = p.PictAddExclusion(task, excl2_ref, EXCLUSION_2_SIZE);

        // 必ず行う組み合わせ
        int SEED_1_SIZE = 5;
        PICT_EXCLUSION_ITEM.ByReference seed1_ref = new PICT_EXCLUSION_ITEM.ByReference();
        PICT_EXCLUSION_ITEM[] seed1 = (PICT_EXCLUSION_ITEM[])seed1_ref.toArray(SEED_1_SIZE);
        seed1[0].Parameter = p1;
        seed1[0].ValueIndex = 1;
        seed1[1].Parameter = p2;
        seed1[1].ValueIndex = 1;
        seed1[2].Parameter = p3;
        seed1[2].ValueIndex = 1;
        seed1[3].Parameter = p4;
        seed1[3].ValueIndex = 1;
        seed1[4].Parameter = p5;
        seed1[4].ValueIndex = 1;

        ret = p.PictAddSeed(task, seed1_ref, SEED_1_SIZE);

        // 生成
        ret = p.PictGenerate(task);
        IntByReference row_ref = p.PictAllocateResultBuffer(task);

        int paramCount = p.PictGetTotalParameterCount( task );
        p.PictResetResultFetching( task );

        int count = 0;
        while(p.PictGetNextResultRow(task, row_ref) > 0){
            Pointer p_row_ref = row_ref.getPointer();
            for(int index = 0; index < paramCount*8; index+=8){
                System.out.print(p_row_ref.getInt(index) + " ");
            }
            System.out.println();
            count++;
        }
        System.out.println();
        System.out.println("This test case times: " + count);
    }
}

