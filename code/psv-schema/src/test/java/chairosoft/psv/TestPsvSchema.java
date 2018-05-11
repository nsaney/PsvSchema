package chairosoft.psv;

import org.junit.Test;

public class TestPsvSchema {
    
    @Test
    public void test_Schema01() throws Exception {
        String fileSchema01 = "src/test/resources/schemas/Schema01.psv";
        String outputSourceRoot = "target/generated-test-sources";
        String packageSchema01 = "chairosoft.test.schema01";
        PsvSchema.main(new String[] { fileSchema01, outputSourceRoot, packageSchema01 });
    }
    
}

